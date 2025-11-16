package com.allura.currencyconverter;

import com.google.gson.Gson; // Se importa Gson para manejar JSON.
import com.google.gson.JsonObject; // Necesitaremos esto para acceder a los datos.
import com.google.gson.JsonSyntaxException; // Para manejo de errores de JSON.

import java.io.IOException;
import java.net.URI; // Construcción de la URL.
import java.net.http.HttpClient; // Cliente HTTP nativo de Java. Nota: Si se desea puede ser cambiado por «Apache».
import java.net.http.HttpRequest; // Construccción de la solicitud HTTP.
import java.net.http.HttpResponse; // Recepción de la respuesta HTTP.
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; // Entrada de usuario.

public class CurrencyConverterApp {

    // ¡IMPORTANTE! REEMPLAZA "TU_API_KEY_AQUÍ" con tu clave API real de ExchangeRate-API.
    private static final String API_KEY = "TU_API_KEY_AQUÍ";
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";

    private static String lastBaseCurrency = null; //Variable de salida
    private static String lastTargetCurrency = null; //Variable de salida
    private static double lastConvertedAmount = 0.0; //Variable de salida
    private static boolean lastConversionSuccessful = false; //Variable de salida



    // Lista y nombre de salida para generar archivo de texto que contendrá el historial de conversión.
    /** ***** Lista para almacenar objetos ConversionRecord ***** **/
    private static final List<ConversionRecord> conversionHistory = new ArrayList<>();
    /** ***** Nombre del archivo para guardar el historial ***** **/



    public static void main(String[] args) {
        System.out.println("--- ¡Bienvenido al Conversor de Monedas Alura ONE! ---");
        Scanner scanner = new Scanner(System.in);

        // Opciones de conversión predefinidas (Opcional agregar más).
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "MXN", "ARS", "BRL", "CLP", "COP", "PEN"};

        while (true) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Convertir moneda (ingresar el monto manualmente)");
            System.out.println("2. Ver opciones de monedas disponibles");
            System.out.println("La opción numero 3 se mostrará después de hacer una conversión. Por favor continúe.");
            if (lastConversionSuccessful) {
                System.out.println("3. Convertir Inversamente (la última conversión)");
            }
            System.out.println("4. Salir");

            System.out.print("Ingrese su opción: ");

            String option = scanner.nextLine();


            switch (option) {
                case "1":
                    String baseCurrency;
                    String targetCurrency;
                    double amount;

                    while (true) {
                        System.out.print("Ingrese la moneda base (ej. USD, EUR, ARS): ");
                        baseCurrency = scanner.nextLine().toUpperCase();
                        if (containsOnlyLetters(baseCurrency) && baseCurrency.length() == 3) {
                            break; // Si es válido, sale del bucle de entrada.
                        } else {
                            System.err.println("Error: El código de la moneda base debe ser un texto de 3 letras (ej. USD). Intente de nuevo por favor.");
                        }
                    }

                    while (true) {
                        System.out.print("Ingrese la moneda de destino (ej. USD, EUR, ARS): ");
                        targetCurrency = scanner.nextLine().toUpperCase();
                        if (containsOnlyLetters(targetCurrency) && targetCurrency.length() == 3) {
                            break; // Si es válido, sale del bucle de entrada.
                        } else {
                            System.err.println("Error: El código de la moneda de destino debe ser un texto de 3 letras (ej. EUR). Intente de nuevo por favor.");
                        }
                    }

                    while (true) { // Este bucle asegura que se solicite la cantidad hasta que sea válida.
                        System.out.print("Por favor ingrese la cantidad a convertir: ");
                        try {
                            amount = Double.parseDouble(scanner.nextLine());
                            // Opcional: Añadir una validación para que la cantidad sea positiva
                            // if (amount <= 0) {
                            //     System.err.println("Error: La cantidad debe ser un número positivo. Intente de nuevo.");
                            //     continue; // Vuelve a pedir la cantidad.
                            // }
                            break; // Si se ingresa un número válido, sale de este bucle interno.
                        } catch (NumberFormatException e) {
                            System.err.println("Cantidad inválida. Por favor, ingrese un número válido e intente de nuevo.");
                        }
                    }

                    performConversion(baseCurrency, targetCurrency, amount);
                    break;
                case "2":
                    System.out.println("\n--- Monedas Comunes Soportadas ---");
                    for (String currency : currencies) {
                        System.out.print(currency + " ");
                    }
                    System.out.println("\n(Para una lista completa, consulte la documentación de ExchangeRate-API)");
                    break;
                case "3":
                    if (lastConversionSuccessful) {
                        System.out.printf("\nRealizando conversión inversa: %.2f %s a %s\n",
                                lastConvertedAmount, lastTargetCurrency, lastBaseCurrency);
                        // Intercambia las monedas y usa la cantidad convertida como la nueva cantidad base
                        performConversion(lastTargetCurrency, lastBaseCurrency, lastConvertedAmount);
                    } else {
                        System.out.println("No hay una conversión exitosa anterior para invertir.");
                    }
                    break;
                case "4":
                    System.out.println("Gracias por usar el conversor. ¡Hasta luego!");
                    HistoryManager historyManager = new HistoryManager();
                    historyManager.saveHistory(conversionHistory);
                    scanner.close();
                    return; // Termina el programa.
                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
            }
        }
    }

    private static boolean containsOnlyLetters(String text) { // NOTA: Parámetro es «Text», más genérico.
        if (text == null || text.isEmpty()) {
            return false; // Si es nulo o vacío, no contiene solo letras.
        }
        // Verifica si toda la cadena coincide con un patrón de solo letras
        return text.matches("[a-zA-Z]+");
    }

    // Metodo para realizar la conversión.
    private static void performConversion(String baseCurrency, String targetCurrency, double amount) {
        String apiUrl = API_BASE_URL + API_KEY + "/latest/" + baseCurrency;

        System.out.println("Conectando a la API: " + apiUrl); // SOUT opcional, se puede omitir.

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonResponse = response.body();

                Gson gson = new Gson();
                try {
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    String resultStatus = jsonObject.get("result").getAsString();

                    if ("success".equals(resultStatus)) {
                        JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

                        if (conversionRates.has(targetCurrency)) {
                            double rate = conversionRates.get(targetCurrency).getAsDouble();
                            double convertedAmount = amount * rate;

                            // Crea un nuevo registro de conversión usando el «Record».
                            ConversionRecord record = new ConversionRecord(
                                    baseCurrency, targetCurrency, amount, convertedAmount, rate
                            );

                            System.out.print("\n--- Resultado de Conversión ---\n");
                            System.out.println(record.toString());
                            System.out.println("------------------------------");

                            // Añade el objeto ConversionRecord a la lista del historial.
                            conversionHistory.add(record);

                            lastBaseCurrency = baseCurrency; // Línea para la conversión inversa.
                            lastTargetCurrency = targetCurrency; // Línea para la conversión inversa.
                            lastConvertedAmount = convertedAmount; // Línea para la conversión inversa.
                            lastConversionSuccessful = true; // Línea para la conversión inversa.

                        } else {
                            System.err.println("Error: No se encontró la tasa de cambio para '" + targetCurrency + "'. Verifique el código de la moneda.");
                            lastConversionSuccessful = false;
                        }
                    } else {
                        // Manejo de errores de la API (ej. clave API inválida, moneda no encontrada, etc.)
                        String errorType = jsonObject.has("error-type") ? jsonObject.get("error-type").getAsString() : "Error desconocido";
                        System.err.println("Error en la respuesta de la API: " + errorType);
                        if ("unsupported-code".equals(errorType)) {
                            System.err.println("Asegúrese de que las monedas base o destino sean códigos ISO 4217 válidos.");
                        } else if ("invalid-key".equals(errorType)) {
                            System.err.println("Verifique que su API_KEY sea correcta y esté activa.");
                        } else if ("malformed-request".equals(errorType) || "invalid-url".equals(errorType)) {
                            System.err.println("La URL de la solicitud está mal formada. Revise la construcción de la URL.");
                            lastConversionSuccessful = false;
                        }
                    }

                } catch (JsonSyntaxException e) {
                    System.err.println("Error al parsear el JSON de la API. La respuesta no es un JSON válido.");
                    System.err.println("Detalle: " + e.getMessage());
                    lastConversionSuccessful = false;
                }

            } else {
                System.err.println("Error HTTP: " + response.statusCode() + " - No se pudo obtener una respuesta exitosa de la API.");
                System.err.println("Cuerpo de la respuesta: " + response.body());
                lastConversionSuccessful = false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error de conexión o interrupción durante la solicitud HTTP.");
            System.err.println("Detalle: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción.
                lastConversionSuccessful = false;
            }
        }
    }
}