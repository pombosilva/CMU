package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import java.util.Locale;

import pt.ulisboa.tecnico.cmov.project.R;

public class UserFragment extends Fragment {

    public UserFragment() {
        // Empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        Button languageButton = rootView.findViewById(R.id.change_language);
        languageButton.setOnClickListener(this::changeLanguage);

        return rootView;
    }

    public void changeLanguage(View view){
        Configuration config = getResources().getConfiguration();
        String currentLocale = config.getLocales().get(0).getLanguage();
        Locale locale;
        if ("en".equals(currentLocale))
            locale = new Locale("pt", "PT");
        else
            locale = new Locale("en", "US");
        Locale.setDefault(locale);
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        requireActivity().recreate();
    }
}