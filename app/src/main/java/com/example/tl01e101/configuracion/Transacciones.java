package com.example.tl01e101.configuracion;

public class Transacciones
{
    // Nombre de la bd
    public static final String NameDatabase = "Agenda";

    // Creacion de tabla y objetos
    public static final String tabla_concacto = "Contacto";

    /* Campos de la tabla personas */
    public static String id = "id";

    public static String pais = "pais";
    public static String nombre = "nombre";
    public static String telefono = "telefono";
    public static String nota = "nota";

    public static String imagen = "imagen";

    // Consultas SQL DDL
    public static String CreateTBContacto = "CREATE TABLE \"Contacto\" (\n" +
            "\t\"id\"\tINTEGER NOT NULL,\n" +
            "\t\"pais\"\tTEXT NOT NULL,\n" +
            "\t\"nombre\"\tTEXT NOT NULL,\n" +
            "\t\"telefono\"\tTEXT NOT NULL,\n" +
            "\t\"nota\"\tTEXT NOT NULL,\n" +
            "\t\"imagen\"\tBLOB NOT NULL,\n" +
            "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
            ");";

    public static String DropTBContacto = "DROP TABLE IF EXISTS Contacto";
}
