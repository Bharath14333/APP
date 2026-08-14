package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;

public class LiveAlertsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        Toolbar toolbar = new Toolbar(this);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (56 * getResources().getDisplayMetrics().density)));
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary));
        toolbar.setTitle("Live Alerts");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbar.setNavigationOnClickListener(v -> finish());

        layout.addView(toolbar);

        FrameLayout container = new FrameLayout(this);
        container.setId(View.generateViewId());
        container.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.addView(container);

        setContentView(layout);

        getSupportFragmentManager().beginTransaction()
            .replace(container.getId(), new AlertsFragment())
            .commit();
    }
}
