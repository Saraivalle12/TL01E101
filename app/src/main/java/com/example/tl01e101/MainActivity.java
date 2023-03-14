package com.example.tl01e101;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tl01e101.configuracion.SQLiteConexion;
import com.example.tl01e101.configuracion.Transacciones;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    iniciarProcedimiento_1();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button btnPicker = findViewById(R.id.btnPicker);

        btnPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    iniciarProcedimiento_2();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnContactosSalvados = findViewById(R.id.btnContactosGuardados);

        btnContactosSalvados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    iniciarProcedimiento_3();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Spinner spinnerNombres = findViewById(R.id.paises);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.paises, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNombres.setAdapter(adapter);
    }


    private void iniciarProcedimiento_1() {
        EditText edtNombre = findViewById(R.id.edtNombre);
        EditText edtTelefono = findViewById(R.id.edtTelefono);
        EditText edtNota = findViewById(R.id.edtNota);
        Spinner paises = findViewById(R.id.paises);

        String nombre = edtNombre.getText().toString();
        String telefono = edtTelefono.getText().toString();
        String nota = edtNota.getText().toString();
        String pais = (String) paises.getSelectedItem();

        if (!esValido(nombre,
                telefono,
                nota)) {
            return;
        }

        // Obtener el ImageView
        ImageView imageView = findViewById(R.id.imgPersona);

        // Obtener el Drawable de la imagen
        Drawable drawable = imageView.getDrawable();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        Contacto contacto = new Contacto(pais, nombre, telefono, nota, comprimir(bitmap));

        if (guardar(contacto)) {
            Toast.makeText(this, "Ingresado con exito", Toast.LENGTH_SHORT).show();

            limpiar();
        } else {
            Toast.makeText(this, "IHa fallado el ingreso, inténtelo nuevamente.", Toast.LENGTH_SHORT).show();

        }
    }

    private void iniciarProcedimiento_2() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Si la versión de Android es menor que la API 22 (Lollipop 5.1), solicitar el permiso
            // Verificar si el permiso para leer el almacenamiento externo ha sido otorgado
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no ha sido otorgado, solicitarlo al usuario en tiempo de ejecución
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // Si el permiso es otorgado, crear un Intent para abrir la galería de imágenes
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                // Iniciar la actividad de selección de archivos
                startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
            }
        } else {
            // Si el permiso es otorgado, crear un Intent para abrir la galería de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");

            // Iniciar la actividad de selección de archivos
            startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
        }
    }

    private void iniciarProcedimiento_3(){
        Intent intent = new Intent(this, ActivityLista.class);

        startActivity(intent);
    }
    private boolean guardar(Contacto contacto) {
        boolean band = false;

        try {
            SQLiteConexion conexion = new SQLiteConexion(this,
                    Transacciones.NameDatabase,
                    null,
                    1);

            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();

            valores.put(Transacciones.nombre, contacto.getNombre());
            valores.put(Transacciones.telefono, contacto.getTelefono());
            valores.put(Transacciones.pais, contacto.getPais());
            valores.put(Transacciones.nota, contacto.getNota());
            valores.put(Transacciones.imagen, contacto.getImagen());

            Long id = db.insert(Transacciones.tabla_concacto, Transacciones.id, valores);

            if (id > 0){
                band = true;
            }
        } catch (Exception ex) {
            ex.toString();
        }

        return band;
    }

    private boolean esValido(String nombre, String telefono, String nota) {
        boolean band = true;

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Debe escribir un nombre!", Toast.LENGTH_SHORT).show();
            band = false;
        }

        if (telefono.isEmpty()) {
            Toast.makeText(this, "Debe escribir un teléfono!", Toast.LENGTH_SHORT).show();
            band = false;
        }

        if (nota.isEmpty()) {
            Toast.makeText(this, "Debe escribir una nota!", Toast.LENGTH_SHORT).show();
            band = false;
        }

        String regex = "^\\s*(\\+(\\d{1,3}))?\\s*(\\d{1,4}\\s*){1,3}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(telefono);

        if (matcher.matches()) {

        } else {
            band = false;
            Toast.makeText(this, "Debe escribir un número telefónico correcto!", Toast.LENGTH_SHORT).show();
        }

        return band;
    }

    // Manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso es otorgado, crear un Intent para abrir la galería de imágenes
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                // Iniciar la actividad de selección de archivos
                startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
            } else {
                // Si el permiso es denegado, mostrar un mensaje al usuario o tomar otra acción
                Toast.makeText(this, "Permiso denegado para leer el almacenamiento externo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Obtener la imagen seleccionada y manipularla
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Obtener la imagen seleccionada
            Uri uri = data.getData();

            try {
                // Convertir la imagen seleccionada a un objeto Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                ImageView imageView = findViewById(R.id.imgPersona);
                // Manipular la imagen según sea necesario
                Drawable drawable = new BitmapDrawable(getResources(), bitmap); // Convierte el objeto Bitmap en un objeto Drawable

                imageView.setImageDrawable(drawable);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] comprimir(Bitmap imagen) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imagen.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imagenBytes = stream.toByteArray();

        return imagenBytes;
    }

    private void limpiar(){
        EditText edtNombre = findViewById(R.id.edtNombre);
        EditText edtTelefono = findViewById(R.id.edtTelefono);
        EditText edtNota = findViewById(R.id.edtNota);
        Spinner paises = findViewById(R.id.paises);
        ImageView imagen = findViewById(R.id.imgPersona);

        edtNombre.setText("");
        edtTelefono.setText("");
        edtNota.setText("");

        paises.setSelection(0);

        imagen.setImageResource(R.drawable.user_default);

    }
}