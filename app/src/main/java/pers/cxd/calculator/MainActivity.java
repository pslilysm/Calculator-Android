package pers.cxd.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SimpleTextWatcher {

    private static final String TAG = "DEBUG_CXD_MainActivity";

    TextView tv_formula;
    HorizontalScrollView hsv_formula;
    TextView tv_ret;

    final Runnable mAutoScrollToEndTask = new Runnable() {
        @Override
        public void run() {
            hsv_formula.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fullScreen();
        setupView();
    }

    private void fullScreen() {
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flag);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void setupView() {
        findViewById(R.id.tv_00).setOnClickListener(this);
        findViewById(R.id.tv_0).setOnClickListener(this);
        findViewById(R.id.tv_dot).setOnClickListener(this);
        findViewById(R.id.tv_1).setOnClickListener(this);
        findViewById(R.id.tv_2).setOnClickListener(this);
        findViewById(R.id.tv_3).setOnClickListener(this);
        findViewById(R.id.tv_4).setOnClickListener(this);
        findViewById(R.id.tv_5).setOnClickListener(this);
        findViewById(R.id.tv_6).setOnClickListener(this);
        findViewById(R.id.tv_7).setOnClickListener(this);
        findViewById(R.id.tv_8).setOnClickListener(this);
        findViewById(R.id.tv_9).setOnClickListener(this);
        findViewById(R.id.tv_equal).setOnClickListener(this);
        findViewById(R.id.tv_add).setOnClickListener(this);
        findViewById(R.id.tv_minus).setOnClickListener(this);
        findViewById(R.id.tv_x).setOnClickListener(this);
        findViewById(R.id.tv_divide).setOnClickListener(this);
        findViewById(R.id.tv_c).setOnClickListener(this);
        findViewById(R.id.tv_percent).setOnClickListener(this);
        findViewById(R.id.fl_backspace).setOnClickListener(this);

        tv_formula = findViewById(R.id.tv_formula);
        hsv_formula = findViewById(R.id.hsv_formula);
        tv_ret = findViewById(R.id.tv_ret);

        tv_formula.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        String curFormula = tv_formula.getText().toString();
        switch (v.getId()) {
            case R.id.tv_dot:
                if (TextUtils.isEmpty(curFormula)) {
                    tv_formula.setText("0.");
                } else {
                    char last = curFormula.charAt(curFormula.length() - 1);
                    if (isMathSymbol(last)) {
                        tv_formula.setText(curFormula + "0" + ((TextView) v).getText().toString());
                    } else {
                        tv_formula.setText(curFormula + ((TextView) v).getText().toString());
                    }
                }
                break;
            case R.id.tv_c:
                tv_formula.setText("");
                break;
            case R.id.fl_backspace:
                if (!TextUtils.isEmpty(curFormula)) {
                    tv_formula.setText(curFormula.substring(0, curFormula.length() - 1));
                }
                break;
            case R.id.tv_percent:
                if (!TextUtils.isEmpty(curFormula)) {
                    char last = curFormula.charAt(curFormula.length() - 1);
                    if (Character.isDigit(last)) {
                        tv_formula.setText(curFormula + ((TextView) v).getText().toString());
                    }
                }
                break;
            case R.id.tv_equal:
                CharSequence ret = tv_ret.getText();
                if (!TextUtils.isEmpty(ret)) {
                    tv_formula.setText(ret);
                    tv_ret.setText("");
                }
                break;
            default:
                char c = ((TextView) v).getText().charAt(0);
                if (isMathSymbol(c)){
                    if (TextUtils.isEmpty(curFormula)) {
                        break;
                    } else {
                        char last = curFormula.charAt(curFormula.length() - 1);
                        if (isMathSymbol(last)) {
                            tv_formula.setText(curFormula.substring(0, curFormula.length() - 1) + c);
                            break;
                        }
                    }
                }
                tv_formula.setText(curFormula + ((TextView) v).getText());
        }
        hsv_formula.post(mAutoScrollToEndTask);
    }

    private boolean isMathSymbol(char c){
        return c == '+' || c == '–' || c == '×' || c == '÷';
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(s)){
            tv_ret.setText(doCalculate(s.toString().replaceAll("%", "÷100")));
        }else {
            tv_ret.setText("");
        }
    }

    private String doCalculate(CharSequence c) {
        if (isMathSymbol(c.charAt(c.length() - 1))) {
            // 返回上一次的计算结果
            return tv_ret.getText().toString();
        }else {
            List<CharSequence> list = new ArrayList<>();
            int lastI = 0;
            for (int i = 0; i < c.length(); i++) {
                if (isMathSymbol(c.charAt(i))){
                    list.add(c.subSequence(lastI, i));
                    list.add(Character.toString(c.charAt(i)));
                    lastI = i + 1;
                }
                if (i == c.length() - 1){
                    list.add(c.subSequence(lastI, c.length()));
                }
            }

            List<CharSequence> hpcList = new ArrayList<>();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                CharSequence charSequence = list.get(i);
                char symbol = charSequence.charAt(0);
                if (isMathSymbol(symbol)){
                    switch (symbol) {
                        case '×':
                            String ret = doX(hpcList.get(hpcList.size() - 1).toString(), list.get(i + 1).toString());
                            removeLast(hpcList, hpcList.get(hpcList.size() - 1));
                            hpcList.add(ret);
                            i++;
                            continue;
                        case '÷':
                            String ret1 = doDivide(hpcList.get(hpcList.size() - 1).toString(), list.get(i + 1).toString());
                            removeLast(hpcList, hpcList.get(hpcList.size() - 1));
                            hpcList.add(ret1);
                            i++;
                            continue;
                    }
                }
                hpcList.add(charSequence);
            }

            size = hpcList.size();
            List<CharSequence> retList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                CharSequence charSequence = hpcList.get(i);
                char symbol = charSequence.charAt(0);
                if (isMathSymbol(symbol)){
                    switch (symbol) {
                        case '+':
                            String ret = doAdd(retList.get(retList.size() - 1).toString(), hpcList.get(i + 1).toString());
                            removeLast(retList, retList.get(retList.size() - 1));
                            retList.add(ret);
                            i++;
                            continue;
                        case '–':
                            String ret1 = doMinus(retList.get(retList.size() - 1).toString(), hpcList.get(i + 1).toString());
                            removeLast(retList, retList.get(retList.size() - 1));
                            retList.add(ret1);
                            i++;
                            continue;
                    }
                }
                retList.add(charSequence);
            }

            String ret = retList.get(0).toString();
            if (ret.endsWith(".0")) {
                return ret.substring(0, ret.length() - 2);
            }else {
                return ret;
            }
        }
    }

    private String doAdd(String a, String b){
        return String.valueOf(Double.parseDouble(a) + Double.parseDouble(b));
    }


    private String doMinus(String a, String b){
        return String.valueOf(Double.parseDouble(a) - Double.parseDouble(b));
    }

    private String doX(String a, String b){
        return String.valueOf(Double.parseDouble(a) * Double.parseDouble(b));
    }


    private String doDivide(String a, String b){
        return String.valueOf(Double.parseDouble(a) / Double.parseDouble(b));
    }


    private <T> void removeLast(List<T> list, T at){
        int index = -1;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).equals(at)){
                index = i;
            }
        }
        if (index != -1){
            list.remove(index);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            fullScreen();
        }
    }
}