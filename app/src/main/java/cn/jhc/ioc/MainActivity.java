package cn.jhc.ioc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.BindView;

import cn.jhc.ioc_api.ViewInjector;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.sample_text)
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInjector.injectView(this);
        text.setText("模仿ButterKnife，依赖注入");
    }
}
