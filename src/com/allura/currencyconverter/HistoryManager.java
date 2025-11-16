package com.allura.currencyconverter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Clase para crear un historial de cada consulta, así como crear el nombre de forma automatizada.

public class HistoryManager {

    public void saveHistory(List<ConversionRecord> history) {

        // Se crea un formateador para la fecha y hora en el nombre del archivo.
        // Esto se hace CADA VEZ que se llama a saveHistory.
        DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        // Obtiene la fecha y hora actual para el nombre del archivo (del momento exacto del guardado).
        String currentDateTimeForFileName = LocalDateTime.now().format(fileFormatter);

        // Construir el nombre completo del archivo con el timestamp del momento de guardar.
        String fileName = "conversion_history_" + currentDateTimeForFileName + ".txt";

        // Se usa 'try-with-resources' para asegurar que el PrintWriter se cierre automáticamente.
        // Al no pasar 'true' en FileWriter, se crea un nuevo archivo con este nombre único.
        // O se sobrescribe si, por una extraña coincidencia, ya existiera un archivo con el mismo nombre exacto.
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("--- Historial de Conversión del " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ---");

            // Se recorre la lista de registros de conversión.
            for (ConversionRecord record : history) {
                writer.println(record.toString()); // Se escribe cada registro en una nueva línea.
            }

            writer.println("--- Fin del Historial ---"); // Pie del archivo.

            // Se imprime en consola dónde se guardó el historial.
            System.out.println("\nHistorial de conversiones guardado en: " + fileName);
        } catch (IOException e) {
            // Manejo de errores si hay problemas al escribir el archivo
            System.err.println("Error al guardar el historial en el archivo: " + e.getMessage());
        }
    }
}