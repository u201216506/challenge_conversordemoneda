package com.allura.currencyconverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat; // ///////////// NUEVA LÍNEA /////////////
import java.util.Locale;      // ///////////// NUEVA LÍNEA /////////////

// Clase para guardar un «Historial» en formato txt de todas las consulta en una sesión.

public class ConversionRecord {
    private final String baseCurrency;
    private final String targetCurrency;
    private final double originalAmount;
    private final double convertedAmount;
    private final double conversionRate;
    private final LocalDateTime timestamp;

    public ConversionRecord(String baseCurrency, String targetCurrency, double originalAmount, double convertedAmount, double conversionRate) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.originalAmount = originalAmount;
        this.convertedAmount = convertedAmount;
        this.conversionRate = conversionRate;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Creación de un formateador de números para el Locale de EE.UU. (coma para miles, punto para decimales).
        NumberFormat amountFormatter = NumberFormat.getNumberInstance(Locale.US);
        amountFormatter.setMinimumFractionDigits(2); // Mínimo 2 decimales.
        amountFormatter.setMaximumFractionDigits(2); // Máximo 2 decimales (para cantidades de moneda).

        // Creación de formateador para la tasa de conversión (más decimales para precisión).
        NumberFormat rateFormatter = NumberFormat.getNumberInstance(Locale.US);
        rateFormatter.setMinimumFractionDigits(4); // Mínimo 4 decimales.
        rateFormatter.setMaximumFractionDigits(6); // Máximo 6 decimales (o más si necesitas mayor precisión).

        return String.format(
                "Fecha/Hora: %s\n" +
                        "De: %s %s\n" +
                        "A: %s %s\n" +
                        "Tasa de Conversión: %s",
                timestamp.format(dateTimeFormatter),
                amountFormatter.format(originalAmount), baseCurrency, //Aplicación del formateador
                amountFormatter.format(convertedAmount), targetCurrency, //Aplicación del formateador
                rateFormatter.format(conversionRate) //Aplicación del formateador
        );
    }
}