package hcmute.edu.vn.noicamheo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import hcmute.edu.vn.noicamheo.R;

public class DialFragment extends Fragment {
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9, textView0, textViewStar, textViewSharp, textViewInput;
    ImageButton imageButtonCall, imageButtonDelete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dial, container, false);

        textView0 = view.findViewById(R.id.textView0);
        textView1 = view.findViewById(R.id.textView1);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);
        textView4 = view.findViewById(R.id.textView4);
        textView5 = view.findViewById(R.id.textView5);
        textView6 = view.findViewById(R.id.textView6);
        textView7 = view.findViewById(R.id.textView7);
        textView8 = view.findViewById(R.id.textView8);
        textView9 = view.findViewById(R.id.textView9);
        textViewStar = view.findViewById(R.id.textViewStar);
        textViewSharp = view.findViewById(R.id.textViewSharp);
        textViewInput = view.findViewById(R.id.textViewInput);

        imageButtonCall = view.findViewById(R.id.imageButtonCall);
        imageButtonDelete = view.findViewById(R.id.imageButtonDelete);

        textView0.setOnClickListener(this::clickKeypadEvent);
        textView1.setOnClickListener(this::clickKeypadEvent);
        textView2.setOnClickListener(this::clickKeypadEvent);
        textView3.setOnClickListener(this::clickKeypadEvent);
        textView4.setOnClickListener(this::clickKeypadEvent);
        textView5.setOnClickListener(this::clickKeypadEvent);
        textView6.setOnClickListener(this::clickKeypadEvent);
        textView7.setOnClickListener(this::clickKeypadEvent);
        textView8.setOnClickListener(this::clickKeypadEvent);
        textView9.setOnClickListener(this::clickKeypadEvent);
        textViewSharp.setOnClickListener(this::clickKeypadEvent);
        textViewStar.setOnClickListener(this::clickKeypadEvent);
        imageButtonDelete.setOnClickListener(this::clickKeypadEvent);

        return view;
    }

    private void clickKeypadEvent(View view) {
        String inputText = textViewInput.getText().toString();
        if (view.getId() == R.id.textView0) {
            textViewInput.setText(String.join("", inputText, "0"));
        } else if (view.getId() == R.id.textView1) {
            textViewInput.setText(String.join("", inputText, "1"));
        } else if (view.getId() == R.id.textView2) {
            textViewInput.setText(String.join("", inputText, "2"));
        } else if (view.getId() == R.id.textView3) {
            textViewInput.setText(String.join("", inputText, "3"));
        } else if (view.getId() == R.id.textView4) {
            textViewInput.setText(String.join("", inputText, "4"));
        } else if (view.getId() == R.id.textView5) {
            textViewInput.setText(String.join("", inputText, "5"));
        } else if (view.getId() == R.id.textView6) {
            textViewInput.setText(String.join("", inputText, "6"));
        } else if (view.getId() == R.id.textView7) {
            textViewInput.setText(String.join("", inputText, "7"));
        } else if (view.getId() == R.id.textView8) {
            textViewInput.setText(String.join("", inputText, "8"));
        } else if (view.getId() == R.id.textView9) {
            textViewInput.setText(String.join("", inputText, "9"));
        } else if (view.getId() == R.id.textViewStar) {
            textViewInput.setText(String.join("", inputText, "*"));
        } else if (view.getId() == R.id.textViewSharp) {
            textViewInput.setText(String.join("", inputText, "#"));
        } else if (view.getId() == R.id.imageButtonDelete) {
            inputText = inputText.substring(0, inputText.length() - 1);
            textViewInput.setText(inputText);
        }
    }
}