package com.guarani.pos.electronicinvoice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.guarani.pos.billing.model.BillingConfig;
import com.guarani.pos.electronicinvoice.model.ElectronicInvoice;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.model.SaleDetail;

@Component
public class ElectronicInvoiceXmlBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String buildUnsignedXml(Sale sale,
                                   BillingConfig config,
                                   ElectronicInvoice invoice,
                                   List<SaleDetail> details) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rDE xmlns=\"https://ekuatia.set.gov.py/sifen/xsd\">\n");
        xml.append("  <dVerFor>").append(invoice.getFormatVersion()).append("</dVerFor>\n");
        xml.append("  <DE Id=\"").append(invoice.getCdc()).append("\">\n");
        xml.append("    <dDVId>").append(invoice.getCdcCheckDigit()).append("</dDVId>\n");
        xml.append("    <dFecFirma>").append(sale.getFecha().format(DATE_TIME_FORMAT)).append("</dFecFirma>\n");
        xml.append("    <dSisFact>1</dSisFact>\n");
        xml.append("    <gOpeDE>\n");
        xml.append("      <iTipEmi>").append(invoice.getEmissionTypeCode()).append("</iTipEmi>\n");
        xml.append("      <dDesTipEmi>").append(escapeXml(invoice.getEmissionTypeDescription())).append("</dDesTipEmi>\n");
        xml.append("      <dCodSeg>").append(invoice.getSecurityCode()).append("</dCodSeg>\n");
        xml.append("    </gOpeDE>\n");
        xml.append("    <gTimb>\n");
        xml.append("      <iTiDE>").append(invoice.getDocumentTypeCode()).append("</iTiDE>\n");
        xml.append("      <dDesTiDE>").append(escapeXml(invoice.getDocumentTypeDescription())).append("</dDesTiDE>\n");
        xml.append("      <dNumTim>").append(escapeXml(config.getTimbradoNumber())).append("</dNumTim>\n");
        xml.append("      <dEst>").append(escapeXml(config.getEstablishmentCode())).append("</dEst>\n");
        xml.append("      <dPunExp>").append(escapeXml(config.getExpeditionPoint())).append("</dPunExp>\n");
        xml.append("      <dNumDoc>").append(escapeXml(invoice.getDocumentNumber())).append("</dNumDoc>\n");
        if (invoice.getSeries() != null && !invoice.getSeries().isBlank()) {
            xml.append("      <dSerieNum>").append(escapeXml(invoice.getSeries())).append("</dSerieNum>\n");
        }
        xml.append("      <dFeIniT>").append(sale.getFecha().toLocalDate().format(DATE_FORMAT)).append("</dFeIniT>\n");
        xml.append("      <dFeFinT>").append(escapeXml(config.getTimbradoValidity())).append("</dFeFinT>\n");
        xml.append("    </gTimb>\n");
        xml.append("    <gDatGralOpe>\n");
        xml.append("      <dFeEmiDE>").append(sale.getFecha().format(DATE_TIME_FORMAT)).append("</dFeEmiDE>\n");
        xml.append("      <gEmis>\n");
        xml.append("        <dRucEm>").append(escapeXml(normalizeRucNumber(config.getRuc()))).append("</dRucEm>\n");
        xml.append("        <dDVEmi>").append(escapeXml(extractRucDv(config.getRuc()))).append("</dDVEmi>\n");
        xml.append("        <iTipCont>").append(escapeXml(config.getTaxpayerType())).append("</iTipCont>\n");
        xml.append("        <dNomEmi>").append(escapeXml(config.getLegalName())).append("</dNomEmi>\n");
        xml.append("        <dNomFanEmi>").append(escapeXml(config.getCommercialName())).append("</dNomFanEmi>\n");
        xml.append("        <dDirEmi>").append(escapeXml(config.getAddress())).append("</dDirEmi>\n");
        if (config.getPhone() != null) {
            xml.append("        <dTelEmi>").append(escapeXml(config.getPhone())).append("</dTelEmi>\n");
        }
        xml.append("        <gActEco>\n");
        xml.append("          <cActEco>").append(escapeXml(config.getEconomicActivityCode())).append("</cActEco>\n");
        xml.append("        </gActEco>\n");
        xml.append("      </gEmis>\n");
        xml.append("      <gDatRec>\n");
        appendReceiver(xml, sale);
        xml.append("      </gDatRec>\n");
        xml.append("    </gDatGralOpe>\n");
        xml.append("    <gDtipDE>\n");
        xml.append("      <gCamFE>\n");
        xml.append("        <iNatRec>1</iNatRec>\n");
        xml.append("        <dDesNatRec>Operacion interna</dDesNatRec>\n");
        xml.append("        <iTiOpe>1</iTiOpe>\n");
        xml.append("        <dDesTiOpe>B2C</dDesTiOpe>\n");
        xml.append("        <iTImp>1</iTImp>\n");
        xml.append("        <dDesTImp>IVA</dDesTImp>\n");
        xml.append("        <cMoneOpe>PYG</cMoneOpe>\n");
        xml.append("        <dDesMoneOpe>Guarani</dDesMoneOpe>\n");
        xml.append("      </gCamFE>\n");
        xml.append("      <gCamCond>\n");
        xml.append("        <iCondOpe>1</iCondOpe>\n");
        xml.append("        <dDCondOpe>Contado</dDCondOpe>\n");
        xml.append("      </gCamCond>\n");
        appendItems(xml, details);
        xml.append("    </gDtipDE>\n");
        BigDecimal vat5 = calculateVatBase(details, "IVA_5");
        BigDecimal vat10 = calculateVatBase(details, "IVA_10");
        xml.append("    <gTotSub>\n");
        xml.append("      <dSubExe>0</dSubExe>\n");
        xml.append("      <dSub5>").append(formatAmount(vat5)).append("</dSub5>\n");
        xml.append("      <dSub10>").append(formatAmount(vat10)).append("</dSub10>\n");
        xml.append("      <dTotOpe>").append(sale.getSubtotal().toPlainString()).append("</dTotOpe>\n");
        xml.append("      <dTotDesc>").append(sale.getDescuentoTotal().toPlainString()).append("</dTotDesc>\n");
        xml.append("      <dTotGralOpe>").append(sale.getTotal().toPlainString()).append("</dTotGralOpe>\n");
        xml.append("      <dIVA5>").append(formatAmount(calculateVatAmount(vat5, 5))).append("</dIVA5>\n");
        xml.append("      <dIVA10>").append(formatAmount(calculateVatAmount(vat10, 10))).append("</dIVA10>\n");
        xml.append("      <dTotIVA>").append(formatAmount(calculateVatAmount(vat5, 5).add(calculateVatAmount(vat10, 10)))).append("</dTotIVA>\n");
        xml.append("    </gTotSub>\n");
        xml.append("    <gCamGen>\n");
        xml.append("      <dCarQR>PENDIENTE_FIRMA_Y_HASH_QR</dCarQR>\n");
        xml.append("    </gCamGen>\n");
        xml.append("  </DE>\n");
        xml.append("</rDE>\n");
        return xml.toString();
    }

    private void appendReceiver(StringBuilder xml, Sale sale) {
        if (sale.getCustomer() == null) {
            xml.append("        <iNatRec>1</iNatRec>\n");
            xml.append("        <iTiOpe>1</iTiOpe>\n");
            xml.append("        <dNomRec>Cliente contado</dNomRec>\n");
            xml.append("        <dNumIDRec>0</dNumIDRec>\n");
            return;
        }

        String customerId = sale.getCustomer().getRuc();
        if (customerId == null || customerId.isBlank()) {
            customerId = sale.getCustomer().getDocumento();
        }

        xml.append("        <dNomRec>").append(escapeXml(sale.getCustomer().getNombre())).append("</dNomRec>\n");
        if (customerId != null && !customerId.isBlank()) {
            if (sale.getCustomer().getRuc() != null && !sale.getCustomer().getRuc().isBlank()) {
                xml.append("        <dRucRec>").append(escapeXml(normalizeRucNumber(sale.getCustomer().getRuc()))).append("</dRucRec>\n");
                xml.append("        <dDVRec>").append(escapeXml(extractRucDv(sale.getCustomer().getRuc()))).append("</dDVRec>\n");
            } else {
                xml.append("        <dNumIDRec>").append(escapeXml(customerId)).append("</dNumIDRec>\n");
            }
        }
    }

    private void appendItems(StringBuilder xml, List<SaleDetail> details) {
        int line = 1;
        for (SaleDetail detail : details) {
            xml.append("      <gCamItem>\n");
            xml.append("        <dCodInt>").append(escapeXml(detail.getProductoCodigo())).append("</dCodInt>\n");
            xml.append("        <dDesProSer>").append(escapeXml(detail.getProductoNombre())).append("</dDesProSer>\n");
            xml.append("        <dCantProSer>").append(detail.getCantidad().toPlainString()).append("</dCantProSer>\n");
            xml.append("        <dPUniProSer>").append(detail.getPrecioUnitario().toPlainString()).append("</dPUniProSer>\n");
            xml.append("        <dTotBruOpeItem>").append(detail.getGrossSubtotal().toPlainString()).append("</dTotBruOpeItem>\n");
            xml.append("        <dDescItem>").append(detail.getDiscountAmount().toPlainString()).append("</dDescItem>\n");
            xml.append("        <dValTotOpeItem>").append(detail.getSubtotal().toPlainString()).append("</dValTotOpeItem>\n");
            xml.append("        <gCamIVA>\n");
            xml.append("          <iAfecIVA>").append(resolveVatCode(detail.getVatType())).append("</iAfecIVA>\n");
            xml.append("        </gCamIVA>\n");
            xml.append("        <dNumLinDet>").append(line++).append("</dNumLinDet>\n");
            xml.append("      </gCamItem>\n");
        }
    }

    private BigDecimal calculateVatBase(List<SaleDetail> details, String vatType) {
        return details.stream()
                .filter(detail -> vatType.equalsIgnoreCase(detail.getVatType()))
                .map(detail -> detail.getSubtotal() == null ? BigDecimal.ZERO : detail.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateVatAmount(BigDecimal base, int rate) {
        if (base == null || base.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal divisor = BigDecimal.valueOf(100 + rate);
        return base.multiply(BigDecimal.valueOf(rate)).divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private String resolveVatCode(String vatType) {
        if ("IVA_5".equalsIgnoreCase(vatType)) {
            return "3";
        }
        if ("EXENTO".equalsIgnoreCase(vatType)) {
            return "2";
        }
        return "1";
    }

    private String formatAmount(BigDecimal value) {
        BigDecimal normalized = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        return normalized.stripTrailingZeros().toPlainString();
    }

    private String normalizeRucNumber(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "");
    }

    private String extractRucDv(String value) {
        if (value == null || value.isBlank()) {
            return "0";
        }
        int dashIndex = value.lastIndexOf('-');
        if (dashIndex >= 0 && dashIndex < value.length() - 1) {
            return value.substring(dashIndex + 1).trim();
        }
        return value.substring(value.length() - 1);
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
