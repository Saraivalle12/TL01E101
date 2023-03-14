package com.example.tl01e101;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tl01e101.configuracion.SQLiteConexion;
import com.example.tl01e101.configuracion.Transacciones;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityLista extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CALL = 1;
    private static final int CALL_REQUEST = 2;

    private SQLiteConexion conexion;
    private ListView listapersonas;
    private ArrayList<Contacto> lista;

    private int indice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        indice = -1;
        try {
            iniciarProcedimiento_1();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnVerImagen = findViewById(R.id.btnVerImagen);

        btnVerImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (indice >= 0) {
                    ImageView imageView = new ImageView(ActivityLista.this);
                    imageView.setImageBitmap(convertir(lista.get(indice).getImagen()));
                    int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                    int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLista.this);
                    builder.setView(imageView);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(ActivityLista.this, "Debe selecionar un elemento!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button btnEliminar = findViewById(R.id.btnEliminar);

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (indice >= 0) {

                        SQLiteConexion conexion = new SQLiteConexion(ActivityLista.this,
                                Transacciones.NameDatabase,
                                null,
                                1);

                        SQLiteDatabase db = conexion.getWritableDatabase();
                        // Definir la cláusula WHERE para eliminar el registro deseado
                        String seleccion = "id = ?";
                        String[] argumentosSeleccion = {"" + lista.get(indice).getId()};

                        // Eliminar el registro
                        int cantidadEliminada = db.delete(
                                Transacciones.tabla_concacto,
                                seleccion,
                                argumentosSeleccion
                        );

                        // Verificar si se eliminó correctamente
                        if (cantidadEliminada > 0) {
                            lista.remove(indice);

                            ArrayAdapter adp = new ArrayAdapter(ActivityLista.this, android.R.layout.simple_list_item_1, filllist(lista));
                            listapersonas.setAdapter(adp);
                            indice = -1;

                            Toast.makeText(ActivityLista.this, "Eliminado!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ActivityLista.this, "Hubo un error al eliminar, inténtelo de nuevo!", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Toast.makeText(ActivityLista.this, "Debe seleccionar un elemento!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnCompartir = findViewById(R.id.btnCompartir);

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (indice >= 0){
                    try {
                        // Crear un objeto Intent con la acción ACTION_SEND
                        Intent intent = new Intent(Intent.ACTION_SEND);

                        // Establecer el tipo de datos a text/plain
                        intent.setType("text/plain");

                        // Agregar el string de contacto al intent como texto
                        Contacto obj = lista.get(indice);
                        String contacto = String.format("Nombre del contacto: %s\nNúmero de teléfono: %s", obj.getNombre(), obj.getTelefono());
                        intent.putExtra(Intent.EXTRA_TEXT, contacto);

                        // Iniciar la actividad de compartir
                        startActivity(Intent.createChooser(intent, "Compartir contacto con:"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else{
                    Toast.makeText(ActivityLista.this, "Debe seleccioanr un elemento!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnActualizar = findViewById(R.id.btnActualizar);

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (indice >= 0 && indice < lista.size()){
                   try {
                       Contacto obj = lista.get(indice);

                       Intent intent = new Intent(ActivityLista.this, ActivityActualizar.class);
                       intent.putExtra("id", obj.getId());
                       startActivity(intent);

                       ActivityLista.this.finish();

                   } catch (Exception e){
                       Toast.makeText(ActivityLista.this, "error ", Toast.LENGTH_SHORT).show();
                   }
               } else{
                   Toast.makeText(ActivityLista.this, "Debe selecionar un elemento!", Toast.LENGTH_SHORT).show();
               }
            }
        });

        EditText edtBuscar = findViewById(R.id.edtBuscar);

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se utiliza
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Verificar si se escriben letras
                try {
                    iniciarProcedimiento_2();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se utiliza
            }
        });


    }

    private void iniciarProcedimiento_1() {
        conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
        listapersonas = (ListView) findViewById(R.id.lista);

        ObtenerListaPersonas();

        ArrayAdapter adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, filllist(lista));
        listapersonas.setAdapter(adp);

        listapersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < listapersonas.getChildCount(); i++) {
                    View v = listapersonas.getChildAt(i);
                    v.setBackgroundColor(Color.TRANSPARENT);
                }

                view.setBackgroundColor(Color.YELLOW);

                indice = position;
            }
        });

        listapersonas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Verificar si el permiso para leer el almacenamiento externo ha sido otorgado
                if (ContextCompat.checkSelfPermission(ActivityLista.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Si el permiso no ha sido otorgado, solicitarlo al usuario en tiempo de ejecución
                    ActivityCompat.requestPermissions(ActivityLista.this, new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSIONS_REQUEST_CALL);
                } else {
                    if (indice < 0){
                        Toast.makeText(ActivityLista.this, "Debe seleccionar un elemento!", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    Contacto obj = lista.get(indice);

                    // Crear un objeto Builder para el diálogo
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLista.this);
                    builder.setTitle("Acción");

                    // Establecer el mensaje del diálogo
                    builder.setMessage(String.format("Desea llamar a %s con teléfono : %s", obj.getNombre(), obj.getTelefono()));

                    // Agregar un botón "Aceptar" al diálogo
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // Crear un objeto Intent con la acción ACTION_CALL
                            Intent intent = new Intent(Intent.ACTION_CALL);

                            // Establecer el número de teléfono a llamar
                            String telefono = obj.getTelefono();

                            String str = obj.getPais();
                            Pattern pattern = Pattern.compile("\\((.*?)\\)"); // expresión regular
                            Matcher matcher = pattern.matcher(str);

                            Toast.makeText(ActivityLista.this, str, Toast.LENGTH_SHORT).show();
                            if (matcher.find()) {
                                str = matcher.group(1);
                                telefono = str + telefono;
                            }

                            intent.setData(Uri.parse("tel:" + telefono));

                            // Verificar si la aplicación tiene permiso para realizar llamadas telefónicas
                            if (ActivityCompat.checkSelfPermission(ActivityLista.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // Si la aplicación no tiene permiso, solicitar permiso
                                ActivityCompat.requestPermissions(ActivityLista.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                            } else {
                                // Si la aplicación tiene permiso, iniciar la llamada
                                startActivity(intent);
                            }
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    // Crear el diálogo y mostrarlo
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                return false;
            }
        });
    }

    private void iniciarProcedimiento_2(){
        EditText edtBuscar = findViewById(R.id.edtBuscar);

        String texto = edtBuscar.getText().toString();

        if (texto.isEmpty()){
            ArrayAdapter adp = new ArrayAdapter(ActivityLista.this, android.R.layout.simple_list_item_1, filllist(lista));
            listapersonas.setAdapter(adp);
            indice = -1;
        } else{
            ArrayList<Contacto> l = new ArrayList<>();

            for (Contacto obj : lista){
                if (obj.getNombre().toUpperCase(Locale.ROOT).startsWith(texto.toUpperCase())){
                    l.add(obj);
                }
            }

            ArrayAdapter adp = new ArrayAdapter(ActivityLista.this, android.R.layout.simple_list_item_1, filllist(l));
            listapersonas.setAdapter(adp);
            indice = -1;
        }

    }

    private void ObtenerListaPersonas() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Contacto obj = null;
        lista = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Transacciones.tabla_concacto, null);

        try {
            while (cursor.moveToNext()) {
                obj = new Contacto();
                obj.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Transacciones.id)));
                obj.setPais(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.pais)));
                obj.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.nombre)));
                obj.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.telefono)));
                obj.setNota(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.nota)));
                obj.setImagen(cursor.getBlob(cursor.getColumnIndexOrThrow(Transacciones.imagen)));

                lista.add(obj);
            }
        } catch (Exception e) {
            lista.clear();
            e.printStackTrace();
        }

        cursor.close();

    }

    private ArrayList<String> filllist(ArrayList<Contacto> lista) {
        ArrayList<String> ArregloPersonas = new ArrayList<String>();
        for (int i = 0; i < lista.size(); i++) {
            ArregloPersonas.add((i + 1) + " | " +
                    lista.get(i).getNombre() + " | " +
                    lista.get(i).getPais() + " | "+
                    lista.get(i).getTelefono() + " | ");
        }

        return ArregloPersonas;
    }

    public Bitmap convertir(byte[] v){
        Bitmap bmp = BitmapFactory.decodeByteArray(v, 0, v.length);

        return bmp;
    }
}