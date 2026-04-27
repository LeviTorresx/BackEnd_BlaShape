package com.blashape.backend_blashape.services;

import java.time.Instant;

import com.blashape.backend_blashape.DTOs.InvoiceDTO;
import com.blashape.backend_blashape.entitys.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.PaymentRepository;
import com.blashape.backend_blashape.repositories.PlanRepository;
import com.blashape.backend_blashape.repositories.ProductRepository;
import com.blashape.backend_blashape.repositories.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams.Recurring.Interval;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeService {
    @Value("${stripe.secret-key}")
    private String stripeKey;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private CarpenterRepository carpenterRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Autowired
    private EmailService emailService;


    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    public String createProductStripe(Long productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + productId));

        if (product.getStripePriceId() != null) {
                throw new Exception("El producto ya está creado en Stripe");
        }

        com.stripe.model.Product stripeProduct = com.stripe.model.Product.create(
                com.stripe.param.ProductCreateParams.builder()
                        .setName(product.getProductName())
                        .setDescription(product.getDescription())
                        .build()
        );

        Price price = Price.create(
                com.stripe.param.PriceCreateParams.builder()
                        .setUnitAmount(product.getPrice() * 100)
                        .setCurrency(product.getCurrency())
                        .setProduct(stripeProduct.getId())
                        .build()
        );

        product.setStripePriceId(price.getId());
        productRepository.save(product);

        return price.getId();
    }

    public String createSubscriptionPlanStripe(Long planId) throws Exception {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new Exception("Plan no encontrado con ID: " + planId));

        if (plan.getStripePriceId() != null) {
                throw new Exception("El plan ya está creado en Stripe");
        }

        com.stripe.model.Product stripeProduct = com.stripe.model.Product.create(
                com.stripe.param.ProductCreateParams.builder()
                        .setName(plan.getPlanName())
                        .setDescription(plan.getDescription())
                        .build()
        );

        Price price = Price.create(
                com.stripe.param.PriceCreateParams.builder()
                        .setUnitAmount(plan.getPrice() * 100)
                        .setCurrency(plan.getCurrency())
                        .setProduct(stripeProduct.getId())
                        .setRecurring(
                                com.stripe.param.PriceCreateParams.Recurring.builder()
                                        .setInterval(Interval.valueOf(plan.getInterval().toString()))
                                        .build()
                        )
                        .build()
        );

        plan.setStripePriceId(price.getId());
        planRepository.save(plan);

        return price.getId();
    }

    public String createCheckoutSession(Payment payment, String successUrl, String cancelUrl) throws StripeException {
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("paymentId", String.valueOf(payment.getPaymentId()));

        if (PaymentType.ONE_TIME_PRODUCT.equals(payment.getPaymentType())) {
                builder.setMode(SessionCreateParams.Mode.PAYMENT)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(payment.getProduct().getStripePriceId())
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();
        } else if (PaymentType.SUBSCRIPTION.equals(payment.getPaymentType())) {
                Long activePlanId = subscriptionRepository.getPlanIdForActiveSubscription(
                    payment.getCarpenter().getCarpenterId(), SubscriptionStatus.ACTIVE);

                if (activePlanId != null) {
                        if (activePlanId.equals(payment.getPlan().getPlanId())) {
                                throw new RuntimeException("El carpintero ya está suscrito a este plan");
                        }
                        cancelSubscription(payment.getCarpenter().getCarpenterId());
                }

                builder.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                                .addLineItem(
                                        SessionCreateParams.LineItem.builder()
                                                .setPrice(payment.getPlan().getStripePriceId())
                                                .setQuantity(1L)
                                                .build()
                                );
        } else {
            throw new RuntimeException("Tipo de pago no válido");
        }

        Session session = Session.create(builder.build());
        
        payment.setStripeSessionId(session.getId());
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        return session.getUrl();
    }

    public Payment createPayment(Long id, Long carpenterId, PaymentType paymentType, String description) {
        Payment payment = new Payment();
        payment.setPaymentType(paymentType);
        payment.setDescription(description);

        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new RuntimeException("Carpintero no encontrado con ID: " + carpenterId));

        payment.setCarpenter(carpenter);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        if (PaymentType.ONE_TIME_PRODUCT.equals(paymentType)) {
                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

                if (product.getStripePriceId() == null) {
                        throw new RuntimeException("El producto no está creado en Stripe");
                }
        
                payment.setProduct(product);
                payment.setAmount(product.getPrice());
                payment.setCurrency(product.getCurrency());
        } else if (PaymentType.SUBSCRIPTION.equals(paymentType)) {
                Plan plan = planRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + id));

                if (plan.getStripePriceId() == null) {
                        throw new RuntimeException("El plan no está creado en Stripe");
                }

                payment.setSubscription(null);
                payment.setPlan(plan);
                payment.setAmount(plan.getPrice());
                payment.setCurrency(plan.getCurrency());
        } else {
                throw new RuntimeException("Tipo de pago no válido");
        }

        return paymentRepository.save(payment);
    }

    public void cancelSubscription(Long carpenteId) throws StripeException {
        AppSubscription subscription = subscriptionRepository.findByCarpenter_CarpenterIdAndStatus(
                carpenteId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No se encontró una suscripción activa para el carpintero con ID: " + carpenteId));

        Subscription stripeSub = Subscription.retrieve(subscription.getStripeSubscriptionId());
        stripeSub.cancel();

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
    }

    public void handleCheckoutSessionCompleted(Session session) throws StripeException {
        log.info("Session ID: {}", session.getId());
        log.info("Metadata: {}", session.getMetadata());
        log.info("PaymentIntent: {}", session.getPaymentIntent());
        log.info("Subscription: {}", session.getSubscription());

        String paymentIdString = session.getMetadata().get("paymentId");

        if (paymentIdString == null) {
            log.error("paymentId no encontrado en metadata");
            throw new RuntimeException("paymentId ausente en metadata");
        }

        Long paymentId = Long.parseLong(paymentIdString);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        // Evita reprocesar pagos ya completados
        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setStripePaymentIntent(session.getPaymentIntent());
        payment.setUpdatedAt(Instant.now());

        if (PaymentType.SUBSCRIPTION.equals(payment.getPaymentType())) {

            Subscription stripeSub = Subscription.retrieve(session.getSubscription());

            String paymentIntent = null;

            if (stripeSub.getLatestInvoice() != null) {
                com.stripe.model.Invoice invoice =
                        com.stripe.model.Invoice.retrieve(stripeSub.getLatestInvoice());

                paymentIntent = invoice.getPaymentIntent();
            }

            AppSubscription subscription = new AppSubscription();
            subscription.setCarpenter(payment.getCarpenter());
            subscription.setPlan(payment.getPlan());
            subscription.setStripeSubscriptionId(session.getSubscription());
            subscription.setStripeCustomerId(session.getCustomer());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()));
            subscription.setEndDate(Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()));
            subscription.setRemainingCuts(payment.getPlan().getCuttingLimit());

            subscriptionRepository.save(subscription);

            payment.setSubscription(subscription);
            payment.setStripePaymentIntent(paymentIntent);
        }

        paymentRepository.save(payment);

        // Dentro de handleCheckoutSessionCompleted, después de paymentRepository.save(payment)

        Carpenter carpenter = payment.getCarpenter();
        Workshop workshop  = carpenter.getWorkshop(); // puede ser null si aún no tiene taller

        InvoiceDTO invoiceDTO = InvoiceDTO.builder()
                .invoiceId("FAC-" + payment.getPaymentId())
                .invoiceDate(Instant.now())
                // Cliente
                .customerName(carpenter.getName() + " " + carpenter.getLastName())
                .customerDni(carpenter.getDni())
                .customerEmail(carpenter.getEmail())
                // Taller del carpintero
                .workshopName(workshop != null ? workshop.getName() : "-")
                .workshopAddress(workshop != null ? workshop.getAddress() : "-")
                // Servicio
                .concept(payment.getDescription())
                .periodStart(payment.getSubscription() != null
                        ? payment.getSubscription().getStartDate() : null)
                .periodEnd(payment.getSubscription() != null
                        ? payment.getSubscription().getEndDate() : null)
                .unitCost(payment.getAmount())
                .discountPercent(0.0)
                .vatPercent(19.0)
                .paymentMethod("Pago en linea - Stripe")
                .build();

        byte[] pdf = pdfInvoiceService.generateFormalInvoice(invoiceDTO);

        String subject = PaymentType.SUBSCRIPTION.equals(payment.getPaymentType())
                ? "Tu factura de suscripción - Blashape"
                : "Tu factura de compra - Blashape";

        String htmlBody = String.format("""
    <div style="font-family:Arial,sans-serif; padding:24px; color:#333;">
        <h2 style="color:#9117e4;">¡Gracias por tu pago, %s!</h2>
        <p>Adjunto encontrarás tu factura correspondiente.</p>
        <p>Gracias por confiar en <strong>Blashape</strong>.</p>
    </div>
    """, carpenter.getName());

        emailService.sendEmailWithAttachment(
                carpenter.getEmail(),
                subject,
                htmlBody,
                pdf,
                "factura_" + payment.getPaymentId() + ".pdf"
        );
    }
}
