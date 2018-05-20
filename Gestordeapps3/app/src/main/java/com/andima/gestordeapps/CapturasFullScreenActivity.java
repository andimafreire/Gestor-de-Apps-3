package com.andima.gestordeapps;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CapturasFullScreenActivity extends AppCompatActivity {

    public static int posicion;
    public static List<Bitmap> capturas;
    private TextView contador;

    // representa si la actividad está a pantalla completa(false) o no(true)
    private boolean visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (capturas != null) {
            // se pone la actividad a pantalla completa
            hideSystemUI();
            visible = false;

            setContentView(R.layout.activity_capturas_full_screen);

            ViewPager viewPager = findViewById(R.id.capturas_viewpager);
            contador = findViewById(R.id.contador_capturas);
            CapturasPagerAdapter adapter = new CapturasPagerAdapter();
            viewPager.setAdapter(adapter);
            // Se establece la posción de la captura seleccionada en la actividad anterior
            viewPager.setCurrentItem(posicion);
            // se carga la animación para el cambio de elementos
            viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
            // Se añade el listener para actualizar la numeración de la captura que se muestra
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    String pagina = Integer.toString(position+1)+"/"+Integer.toString(capturas.size());
                    contador.setText(pagina);
                    Log.d("cambio","onPageScrolled: "+pagina);
                }
                @Override
                public void onPageSelected(int position) {}
                @Override
                public void onPageScrollStateChanged(int state) {}
            });

        } else finish();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private class CapturasPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return capturas.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup view, int position) {

            View imageLayout = LayoutInflater.from(CapturasFullScreenActivity.this)
                    .inflate(R.layout.captura_viewpager, view, false);
            ImageView imageView = imageLayout.findViewById(R.id.captura_fullscreen);
            imageView.setImageBitmap(capturas.get(position));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(visible) hideSystemUI();
                    else showSystemUI();
                    visible = !visible;
                }
            });
            view.addView(imageLayout, 0);
            return imageLayout;
        }
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}

