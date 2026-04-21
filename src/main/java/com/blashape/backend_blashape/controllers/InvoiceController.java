package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.InvoiceDTO;
import com.blashape.backend_blashape.DTOs.ReceiptDTO;
import com.blashape.backend_blashape.services.PdfInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api_BS/invoice")
public class InvoiceController {

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @PostMapping("/preview")
    public ResponseEntity<byte[]> previewInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        byte[] pdf = pdfInvoiceService.generateFormalInvoice(invoiceDTO);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preview_factura.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/preview-receipt")
    public ResponseEntity<byte[]> previewReceipt(@RequestBody ReceiptDTO receiptDTO) {
        byte[] pdf = pdfInvoiceService.generateInvoice(receiptDTO);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preview_recibo.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}