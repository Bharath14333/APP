package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.emergency.crisissense.R;

public class IntroActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView btnSkip;

    private final int[] slideIcons = {
        android.R.drawable.ic_dialog_alert,
        android.R.drawable.ic_dialog_info,
        android.R.drawable.ic_menu_myplaces
    };

    private final String[] slideTitles = {
        "Real-time Emergency Reporting",
        "AI-Driven Severity Grading",
        "Community & Volunteer Support"
    };

    private final String[] slideDescriptions = {
        "Report incidents like accidents, fire, and floods instantly to request assistance.",
        "Our system automatically routes and scales incident priority for emergency responders.",
        "Join hands as a volunteer or responder to manage tasks and coordinate rescue operations."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.view_pager);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        IntroAdapter adapter = new IntroAdapter();
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == slideTitles.length - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < slideTitles.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                launchWelcome();
            }
        });

        btnSkip.setOnClickListener(v -> launchWelcome());
    }

    private void launchWelcome() {
        startActivity(new Intent(IntroActivity.this, WelcomeActivity.class));
        finish();
    }

    private class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.SlideViewHolder> {
        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intro_slide, parent, false);
            return new SlideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            holder.imgIcon.setImageResource(slideIcons[position]);
            holder.txtTitle.setText(slideTitles[position]);
            holder.txtDesc.setText(slideDescriptions[position]);
        }

        @Override
        public int getItemCount() {
            return slideTitles.length;
        }

        class SlideViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView txtTitle, txtDesc;

            public SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.img_slide_icon);
                txtTitle = itemView.findViewById(R.id.txt_slide_title);
                txtDesc = itemView.findViewById(R.id.txt_slide_desc);
            }
        }
    }
}
