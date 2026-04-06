package com.blashape.backend_blashape.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Payment;
import com.blashape.backend_blashape.entitys.PaymentStatus;
import com.blashape.backend_blashape.entitys.PaymentType;
import com.blashape.backend_blashape.entitys.Plan;
import com.blashape.backend_blashape.entitys.Product;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.PaymentRepository;
import com.blashape.backend_blashape.repositories.PlanRepository;
import com.blashape.backend_blashape.repositories.ProductRepository;
import com.stripe.Stripe;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams.Recurring.Interval;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

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

    public String createCheckoutSession(Long id, String successUrl, String cancelUrl, PaymentType paymentType) throws Exception {
        if (PaymentType.ONE_TIME_PRODUCT.equals(paymentType)) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));

            if (product.getStripePriceId() == null) {
                throw new Exception("El producto no está creado en Stripe");
            }

            SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(product.getStripePriceId())
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

                Session session = Session.create(params);
                return session.getId();
        } else if (PaymentType.SUBSCRIPTION.equals(paymentType)) {
            Plan plan = planRepository.findById(id)
                    .orElseThrow(() -> new Exception("Plan no encontrado con ID: " + id));

            if (plan.getStripePriceId() == null) {
                throw new Exception("El plan no está creado en Stripe");
            }

            SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(plan.getStripePriceId())
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

                Session session = Session.create(params);
                return session.getId();
        } else {
            throw new Exception("Tipo de pago no válido");
        }
    }

    public Payment createPayment(Long id, Long carpenterId, String stripeSessionId, PaymentType paymentType, String description) throws Exception {
        Payment payment = new Payment();
        payment.setPaymentType(paymentType);
        payment.setDescription(description);

        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new RuntimeException("Carpintero no encontrado con ID: " + carpenterId));

        payment.setCarpenter(carpenter);
        payment.setStripeSessionId(stripeSessionId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        if (PaymentType.ONE_TIME_PRODUCT.equals(paymentType)) {
                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));

                if (product.getStripePriceId() == null) {
                        throw new Exception("El producto no está creado en Stripe");
                }
        
                payment.setProduct(product);
                payment.setAmount(product.getPrice());
                payment.setCurrency(product.getCurrency());
        } else if (PaymentType.SUBSCRIPTION.equals(paymentType)) {
                payment.setSubscription(null);
        } else {
                throw new Exception("Tipo de pago no válido");
        }

        return paymentRepository.save(payment);
    }
}
