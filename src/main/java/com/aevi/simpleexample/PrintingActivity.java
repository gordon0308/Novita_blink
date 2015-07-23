/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.helpers.services.AeviServiceException;
import com.aevi.printing.PrintCallbackListener;
import com.aevi.printing.PrintJobStateChangedEvent;
import com.aevi.printing.PrintService;
import com.aevi.printing.PrintServiceProvider;
import com.aevi.printing.PrinterSettings;
import com.aevi.printing.PrintingServiceNotInstalledException;
import com.aevi.printing.model.Alignment;
import com.aevi.printing.model.FontStyle;
import com.aevi.printing.model.PrintPayload;
import com.aevi.printing.model.Underline;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The printing sample sends a receipt pay load containing two lines to the
 * print service.
 * <p/>
 * It does so by first connecting a PrintServiceProvider by calling the connect
 * function. Once this service provider has been initialized the PrintService is
 * passed via a callback
 * <p/>
 * After the connection has been established between the activity and the PrintService
 * an event listener is registered. This event listener shows a 'Toast'
 * message each time a print job event is received.
 * <p/>
 * Since the callback thread does not have access to the UI thread 'Looper' we have an intermediate
 * message handler in place that does have access the UI thread.
 * <p/>
 * We send the event data from the callback thread to the handler via a bundle.
 * You can serialize an event into a bundle by using the toBundle function and
 * de-serialize is by using the static fromBundle function.
 * <p/>
 * Releasing the PrintService is done by closing the PrintServiceProvider.
 */
public class PrintingActivity extends Activity {
    private static final String TAG = PrintingActivity.class.getSimpleName();

    private PrintServiceProvider serviceProvider;
    private PrintService service;

    private PrintCallbackListener printCallbackListener = new PrintCallbackListener() {
        @Override
        public void onPrintEvent(PrintJobStateChangedEvent event) {
            showMessage(String.format("Printing event received. Job: %d, status: %s", +event.getTaskId(), event.getStatus()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new PrintServiceProvider(this);
        register();
    }

    @Override
    protected void onPause() {
        // close all services and remove listeners
        unregister();
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }

    private void register() {
        serviceProvider.connect(new AeviServiceConnectionCallback<PrintService>() {
            @Override
            public void onConnect(PrintService printService) {
                if (printService == null) {
                    showMessage("Print service can not be opened, please check the ADB log file for details.");
                    service = null;
                    disableButtons();
                } else {
                    showMessage("Print service initialized");
                    printService.registerUpdateListener(printCallbackListener);
                    service = printService;
                    enableButtons();
                }
            }
        });
    }

    private void unregister() {
        if (service != null) {
            disableButtons();
            service.unregisterUpdateListener(printCallbackListener);
            service = null;
        }
    }

    /**
     * Prints a receipt containing the text 'hello world' and the current date and time
     *
     * @param view the clicked view
     */
    public void printReceiptClick(View view) {
        if (!validateApiIsUsable()) {
            return;
        }

        Log.i(TAG, "printing a receipt");
        try {
            service.print(createCurrentTestPayLoad(service.getDefaultPrinterSettings()));
        } catch (PrintingServiceNotInstalledException e) {
            Log.e(TAG, "Print Service is not installed");
            showMessage("The ANG Printing Service is not installed");
        } catch (AeviServiceException e) {
            Log.e(TAG, "Received exception when printing", e);
            showMessage("Received exception when printing : " + e.getMessage());
        }
    }

    /**
     * Prints a receipt containing several lines of text and images
     *
     * @param view
     */
    public void previewReceiptClick(View view) {
        if (!validateApiIsUsable()) {
            return;
        }

        try {
            Log.i(TAG, "previewing a receipt");
            Intent startIntent = new Intent(this, PrintPreviewActivity.class);
            startIntent.putExtra("payload", createCurrentTestPayLoad(service.getDefaultPrinterSettings()));
            startActivity(startIntent);
        } catch (PrintingServiceNotInstalledException e) {
            Log.e(TAG, "Print Service is not installed");
            showMessage("The ANG Printing Service is not installed");
        } catch (AeviServiceException e) {
            Log.e(TAG, "Received exception when previewing", e);
            showMessage("Received exception when previewing : " + e.getMessage());
        }
    }


    private PrintPayload createCurrentTestPayLoad(PrinterSettings printerSettings) {
        Spinner printExampleSpinner = (Spinner) findViewById(R.id.print_example_spinner);
        String exampleName = printExampleSpinner.getSelectedItem().toString();
        if (exampleName.equals("Example 1")) {
            return payloadTest1(printerSettings);
        } else if (exampleName.equals("Example 2")) {
            return payloadTest2(printerSettings);
        } else if (exampleName.equals("Example 3")) {
            return payloadTest3(printerSettings);
        } else if (exampleName.equals("Example 4")) {
            return payloadTest4(printerSettings);
        } else if (exampleName.equals("Example 5")) {
            return payloadTest5(printerSettings);
        }
        throw new IllegalArgumentException("Unknown example");
    }

    private PrintPayload payloadTest1(PrinterSettings printerSettings) {
        // construct the printer pay load
        PrintPayload printPayload = new PrintPayload();

        // first line, hello world
        printPayload.append("Hello world!").align(Alignment.CENTER);

        // second line, the current date and time
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S", Locale.ENGLISH);
        Date date = new Date(System.currentTimeMillis());
        printPayload.append(String.format("The time is %s", dateFormatter.format(date)))
                .underline(Underline.DOUBLE)
                .fontStyle(FontStyle.EMPHASIZED);

        BitmapFactory.Options bitmapFactoryOptions = printerSettings.asBitmapFactoryOptions();

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.bwlogotrans, bitmapFactoryOptions);
        printPayload.append(image);
        return printPayload;
    }

    private PrintPayload payloadTest2(PrinterSettings printerSettings) {
        // construct the printer pay load
        PrintPayload printPayload = new PrintPayload();

        // text lines
        printPayload.append("Align Left");

        printPayload.append("Align Right").align(Alignment.RIGHT);

        printPayload.append("Align Center").align(Alignment.CENTER);

        printPayload.append("Emphasized").fontStyle(FontStyle.EMPHASIZED);

        printPayload.append("Inverted").fontStyle(FontStyle.INVERTED);

        printPayload.appendEmptyLine();

        printPayload.append("InvertedEmphasized").fontStyle(FontStyle.INVERTED_EMPHASIZED);

        printPayload.append("Single Underlined").underline(Underline.SINGLE);

        printPayload.append("Double Underlined").underline(Underline.DOUBLE);

        printPayload.appendEmptyLine();

        // graphic lines
        BitmapFactory.Options bitmapFactoryOptions = printerSettings.asBitmapFactoryOptions();

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.bwlogotrans, bitmapFactoryOptions);
        printPayload.append(image).align(Alignment.LEFT).contrastLevel(100);

        printPayload.appendEmptyLine();

        printPayload.append(image).align(Alignment.RIGHT);

        printPayload.appendEmptyLine();

        printPayload.append(image).align(Alignment.CENTER).contrastLevel(25);

        return printPayload;
    }

    private PrintPayload payloadTest3(PrinterSettings printerSettings) {

        PrintPayload payload = new PrintPayload();
        // blank line
        payload.appendEmptyLine();
        // logo
        // graphic lines
        BitmapFactory.Options bitmapFactoryOptions = printerSettings.asBitmapFactoryOptions();

        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.yourlogo, bitmapFactoryOptions);
        payload.appendEmptyLine();
        payload.append(logo).align(Alignment.CENTER);
        // blank line
        payload.appendEmptyLine();
        // contact no.
        payload.append("Phone: 1234567890").align(Alignment.CENTER);
        // address
        payload.append("Address: DP1 level6 ");
        // // append a table
        payload.append("#   Name        Price ")
                .underline(Underline.DOUBLE)
                .align(Alignment.CENTER)
                .fontStyle(FontStyle.EMPHASIZED);
        payload.append("1   Doughnut    $1.02 ").align(Alignment.CENTER);
        payload.append("2   Beer        $6.00 ").align(Alignment.CENTER);
        payload.appendEmptyLine();
        payload.append("    Total       $7.02 ").align(Alignment.CENTER).fontStyle(FontStyle.INVERTED_EMPHASIZED);
        payload.appendEmptyLine();
        payload.appendEmptyLine();
        payload.appendEmptyLine();
        return payload;
    }

    private PrintPayload payloadTest4(PrinterSettings printerSettings) {

        PrintPayload payload = new PrintPayload();

        payload.append("Canvas printer test").align(Alignment.CENTER);

        int canvasHeight = 200;
        int margin = 10;
        int width = printerSettings.getPaperWidth() - 100;
        Bitmap bitmap = Bitmap.createBitmap(width, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        paint.setTextSize(50);
        paint.setTextSkewX(-0.25f);
        paint.setAntiAlias(true);

        paint.setColor(Color.BLACK);
        canvas.drawText("Aevi", 60, 80, paint);
        paint.setColor(Color.argb(255, 32, 32, 32)); // Thermal printer dark grey
        canvas.drawText("Aevi", 90, 110, paint);
        paint.setColor(Color.argb(255, 40, 40, 40)); // Thermal printer light grey
        canvas.drawText("Aevi", 120 , 140, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        canvas.drawRect(margin, margin, width - margin, canvasHeight - margin, paint);
        payload.append(bitmap).align(Alignment.CENTER);

        payload.append("End canvas").align(Alignment.CENTER);

        return payload;
    }

    private PrintPayload payloadTest5(PrinterSettings printerSettings) {

        int lineWidth = printerSettings.getPaperWidth();
        Paint paint = new Paint();
        paint.setTextSize(30);
        //create a bitmap that is higher than the text size
        float height = paint.descent() - paint.ascent();
        Bitmap myBitmap = Bitmap.createBitmap(lineWidth, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(myBitmap);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);

        //Text Left and Right Aligned
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("left", 20, height / 2 + 50, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("right", lineWidth - 20, height / 2 + 50, paint);

        //Slanted Parallelogram border on top and bottom
        paint.setColor(Color.BLACK);
        for (int i = 0; i < 22; i++) {
            Point point1Draw = new Point(10 + i * 21, 5);
            Point point2Draw = new Point(5 + i * 21, 10);
            Point point3Draw = new Point(10 + i * 21, 20);
            Point point4Draw = new Point(20 + i * 21, 10);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(point1Draw.x, point1Draw.y);
            path.lineTo(point2Draw.x, point2Draw.y);
            path.lineTo(point3Draw.x, point3Draw.y);
            path.lineTo(point4Draw.x, point4Draw.y);
            path.lineTo(point1Draw.x, point1Draw.y);
            path.close();
            canvas.drawPath(path, paint);

            path.reset();
            path.moveTo(point1Draw.x, 295 + point1Draw.y);
            path.lineTo(point2Draw.x, 295 + point2Draw.y);
            path.lineTo(point3Draw.x, 295 + point3Draw.y);
            path.lineTo(point4Draw.x, 295 + point4Draw.y);
            path.lineTo(point1Draw.x, 295 + point1Draw.y);
            path.close();
            canvas.drawPath(path, paint);
        }

        //3 circles
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("3 circles", 20, 140, paint);
        paint.setAlpha(255);
        canvas.drawCircle(165, 120, 50, paint);
        paint.setAlpha(160);
        canvas.drawCircle(265, 120, 30, paint);
        paint.setAlpha(127);
        canvas.drawCircle(325, 120, 10, paint);

        //Image and text in same line
        BitmapFactory.Options bitmapFactoryOptions = printerSettings.asBitmapFactoryOptions();

        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.bwlogotrans, bitmapFactoryOptions);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(20);
        canvas.drawText("Powered By", 20, 270, paint);
        canvas.drawBitmap(logo, 140, 230, paint);


        //Sample Receipt Text
        paint.setColor(Color.BLACK);
        canvas.save();
        paint.setAlpha(127);
        canvas.rotate(-45);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Sample Receipt", 40, 300, paint);
        canvas.restore();
        PrintPayload printPayload = new PrintPayload();
        printPayload.append(myBitmap);
        return printPayload;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void disableButtons() {
        findViewById(R.id.previewButton).setEnabled(false);
        findViewById(R.id.printButton).setEnabled(false);
    }

    private void enableButtons() {
        findViewById(R.id.previewButton).setEnabled(true);
        findViewById(R.id.printButton).setEnabled(true);
    }


    private boolean validateApiIsUsable() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        if (!terminalConfiguration.isPrintApiUsable()) {
            Toast.makeText(this, "Print API does not work. Reason : " + terminalConfiguration.getPrintApiStatus(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}