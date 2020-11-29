package com.example.painttest3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private MyPaintView myPaintView;
    private ImageView imageViewBrushSize;
    private View viewColor;
    private SeekBar seekBarAlpha;
    private SeekBar seekBarRed;
    private SeekBar seekBarGreen;
    private SeekBar seekBarBlue;
    private String[] stringArrayOfBrushStyles;
    private String downloadedImagePath = "";
    private final int RESULT_IMAGE_DOWNLOAD = 111;
    private int codeOfMaskFilterAlertDialog;
    private Uri uriDownloadedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        myPaintView = findViewById(R.id.myPaintView);
        stringArrayOfBrushStyles = getResources().getStringArray(R.array.select_brush_style);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_color) {
            showDialogSetColor();
        } else if (item.getItemId() == R.id.menu_save) {
            showDialogSaveImage();
        } else if (item.getItemId() == R.id.menu_clear) {
            showDialogClearCanvas();
        } else if (item.getItemId() == R.id.menu_brush_size) {
            showDialogSelectBrushSize();
        } else if (item.getItemId() == R.id.menu_brush_style) {
            showDialogSelectBrushStyle();
        } else if (item.getItemId() == R.id.menu_download) {
            showDialogSaveImageBeforeDownloadingImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_IMAGE_DOWNLOAD && resultCode == RESULT_OK) {
            assert data != null;
            uriDownloadedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver()
                    .query(uriDownloadedImage, filePath, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            downloadedImagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            if (uriDownloadedImage != null) {
                Bitmap bitmap = MyPaintView.getResizeBitmap(downloadedImagePath);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                Bitmap workingBitmap = bitmapDrawable.getBitmap();
                Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

                int width = mutableBitmap.getWidth();
                int height = mutableBitmap.getHeight();

                Canvas canvas = myPaintView.getBitmapCanvas();

                Matrix matrix = new Matrix();

                matrix.setRectToRect(
                        new RectF(0, 0, width, height),
                        new RectF(0, 0, canvas.getWidth(),
                                canvas.getHeight()), Matrix.ScaleToFit.CENTER);

                Bitmap output = Bitmap.createBitmap(mutableBitmap, 0, 0, width, height, matrix, true);

                myPaintView.setBitmap(output);
                myPaintView.setBitmapCanvas(new Canvas(output));

                cursor.close();
            }
        }
    }

    private void showDialogSaveImageBeforeDownloadingImage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_save_image, null);

        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = myPaintView.getBitmap();
                SavePaintImage saveFile = new SavePaintImage();
                saveFile.saveImage(getApplicationContext(), bitmap);
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_IMAGE_DOWNLOAD);
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_IMAGE_DOWNLOAD);
            }
        });
    }

    private void showDialogSaveImage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_save_image, null);

        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = myPaintView.getBitmap();
                SavePaintImage saveFile = new SavePaintImage();
                saveFile.saveImage(getApplicationContext(), bitmap);
                alertDialog.dismiss();
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showDialogSelectBrushStyle() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        TextView textViewTitle = new TextView(this);
        textViewTitle.setText(R.string.brush_style);
        textViewTitle.setGravity(Gravity.CENTER);
        textViewTitle.setPadding(5, 35, 5, 5);
        textViewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        textViewTitle.setTextColor(ContextCompat.getColor(this, R.color.color1));
        textViewTitle.setTextSize(18);

        codeOfMaskFilterAlertDialog = myPaintView.getNumberOfBrushStyleType();

        alertDialogBuilder.setSingleChoiceItems(stringArrayOfBrushStyles,
                codeOfMaskFilterAlertDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                codeOfMaskFilterAlertDialog = 0; // normal
                                break;
                            case 1:
                                codeOfMaskFilterAlertDialog = 1; // emboss
                                break;
                            case 2:
                                codeOfMaskFilterAlertDialog = 2; // blur
                                break;
                            default:
                                break;
                        }
                    }
                });

        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCustomTitle(textViewTitle);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (codeOfMaskFilterAlertDialog) {
                    case 0:
                        myPaintView.setBrushStyleNormal();
                        break;
                    case 1:
                        myPaintView.setBrushStyleEmboss();
                        break;
                    case 2:
                        myPaintView.setBrushStyleBlur();
                        break;
                    default:
                        break;
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showDialogClearCanvas() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_clear_canvas, null);

        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPaintView.clearCanvas();
                alertDialog.dismiss();
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showDialogSetColor() {
        AlertDialog.Builder alertDialogBuilderSetColor = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_brush_color, null);
        seekBarAlpha = view.findViewById(R.id.seekBar_alpha);
        seekBarRed = view.findViewById(R.id.seekBar_red);
        seekBarGreen = view.findViewById(R.id.seekBar_green);
        seekBarBlue = view.findViewById(R.id.seekBar_blue);
        viewColor = view.findViewById(R.id.view_color);

        seekBarAlpha.setOnSeekBarChangeListener(myOnSeekBarChangeListener);
        seekBarRed.setOnSeekBarChangeListener(myOnSeekBarChangeListener);
        seekBarGreen.setOnSeekBarChangeListener(myOnSeekBarChangeListener);
        seekBarBlue.setOnSeekBarChangeListener(myOnSeekBarChangeListener);

        int color = myPaintView.getDrawingColor();
        seekBarAlpha.setProgress(Color.alpha(color));
        seekBarRed.setProgress(Color.red(color));
        seekBarGreen.setProgress(Color.green(color));
        seekBarBlue.setProgress(Color.blue(color));
        viewColor.setBackgroundColor(color);

        alertDialogBuilderSetColor.setView(view);

        alertDialogBuilderSetColor.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilderSetColor.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialogSetColor = alertDialogBuilderSetColor.create();

        alertDialogSetColor.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialogSetColor.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialogSetColor.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialogSetColor.show();

        alertDialogSetColor.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPaintView.setDrawingColor(Color.argb(
                        seekBarAlpha.getProgress(),
                        seekBarRed.getProgress(),
                        seekBarGreen.getProgress(),
                        seekBarBlue.getProgress()));
                alertDialogSetColor.dismiss();
            }
        });

        alertDialogSetColor.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogSetColor.dismiss();
            }
        });
    }

    private final SeekBar.OnSeekBarChangeListener myOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            myPaintView.setBackgroundColor(Color.argb(
                    seekBarAlpha.getProgress(),
                    seekBarRed.getProgress(),
                    seekBarGreen.getProgress(),
                    seekBarBlue.getProgress()));

            viewColor.setBackgroundColor(Color.argb(
                    seekBarAlpha.getProgress(),
                    seekBarRed.getProgress(),
                    seekBarGreen.getProgress(),
                    seekBarBlue.getProgress()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void showDialogSelectBrushSize() {
        AlertDialog.Builder alertDialogBuilderSetWidth = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_brush_size, null);
        SeekBar seekBarBrushSize = view.findViewById(R.id.seekBar_brush_size);
        imageViewBrushSize = view.findViewById(R.id.imageView_brush_size);
        seekBarBrushSize.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBarBrushSize.setProgress(myPaintView.getBrushSize());
        alertDialogBuilderSetWidth.setView(view);

        alertDialogBuilderSetWidth.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilderSetWidth.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialogLineWidth = alertDialogBuilderSetWidth.create();

        alertDialogLineWidth.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialogLineWidth.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
                alertDialogLineWidth.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });

        alertDialogLineWidth.show();

        alertDialogLineWidth.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPaintView.setBrushSize(seekBarBrushSize.getProgress());
                alertDialogLineWidth.dismiss();
                alertDialogLineWidth.dismiss();
            }
        });

        alertDialogLineWidth.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogLineWidth.dismiss();
            }
        });
    }

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        final Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Paint paint = new Paint();
            paint.setColor(myPaintView.getDrawingColor());
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(progress);
            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, paint);
            imageViewBrushSize.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
