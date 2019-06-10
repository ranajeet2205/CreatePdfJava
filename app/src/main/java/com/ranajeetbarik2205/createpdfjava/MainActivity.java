package com.ranajeetbarik2205.createpdfjava;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 0;
    private File pdfFile;
   // private String filename = "Sample.pdf";
    private String filepath = "MyInvoices";

    private Button downloadPdf;
    private BaseFont bfBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAppPermissions();
        //pdfFile = new File(getExternalFilesDir(filepath), filename);

        //pdfFile = new File( new File(Environment.getExternalStorageDirectory(),filepath),filename );
        pdfFile = new File(getAlbumStorageDir(filepath), String.format("pdf-%d.pdf", System.currentTimeMillis()));
        downloadPdf = findViewById(R.id.download_pdf);

        downloadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF("Ranajeet");
            }
        });
    }

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

    }


    private void generatePDF(String personName) {

        //create a new document
        Document document = new Document();

        try {

            PdfWriter docWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();


            PdfContentByte cb = docWriter.getDirectContent();
            //initialize fonts for text printing
            initializeFonts();

            try {
                //the company logo is stored in the assets which is read only
                //get the logo and print on the document
                /*InputStream inputStream = getAssets().open(String.valueOf(R.drawable.ic_launcher_background));
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image companyLogo = Image.getInstance(stream.toByteArray());
                companyLogo.setAbsolutePosition(25, 700);
                companyLogo.scalePercent(25);
                document.add(companyLogo);*/

            } catch (Exception e) {
                e.printStackTrace();
            }


            //creating a sample invoice with some customer data
            createHeadings(cb, 400, 780, "Company Name");
            createHeadings(cb, 400, 765, "Address Line 1");
            createHeadings(cb, 400, 750, "Address Line 2");
            createHeadings(cb, 400, 735, "City, State - ZipCode");
            createHeadings(cb, 400, 720, "Country");

            //list all the products sold to the customer
            float[] columnWidths = {1.5f, 2f, 5f, 2f, 2f};
            //create PDF table with the given widths
            PdfPTable table = new PdfPTable(columnWidths);
            // set table width a percentage of the page width
            table.setTotalWidth(500f);

            PdfPCell cell = new PdfPCell(new Phrase("Qty"));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Item Number"));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Item Description"));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Price"));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Ext Price"));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            table.setHeaderRows(1);

            DecimalFormat df = new DecimalFormat("0.00");
            for (int i = 0; i < 15; i++) {
                double price = Double.valueOf(df.format(Math.random() * 10));
                double extPrice = price * (i + 1);
                table.addCell(String.valueOf(i + 1));
                table.addCell("ITEM" + String.valueOf(i + 1));
                table.addCell("Product Description - SIZE " + String.valueOf(i + 1));
                table.addCell(df.format(price));
                table.addCell(df.format(extPrice));
            }

            //absolute location to print the PDF table from
            table.writeSelectedRows(0, -1, document.leftMargin(), 650, docWriter.getDirectContent());

            //print the signature image along with the persons name
            /*InputStream inputStream = getAssets().open(String.valueOf(R.drawable.ic_launcher_background));
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();
            Image signature = Image.getInstance(bitmapData);
            signature.setAbsolutePosition(400f, 150f);
            signature.scalePercent(25f);
            document.add(signature);*/

            createHeadings(cb, 450, 135, personName);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //PDF file is now ready to be sent to the bluetooth printer using PrintShare
        //File file = new File(filepath+"/Sample.pdf");
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        startActivity(intent);

    }

    private void createHeadings(PdfContentByte cb, float x, float y, String text) {

        cb.beginText();
        cb.setFontAndSize(bfBold, 8);
        cb.setTextMatrix(x, y);
        cb.showText(text.trim());
        cb.endText();

    }

    private void initializeFonts() {


        try {
            bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e("PDF", "Directory not created");
        }
        return file;
    }
}
