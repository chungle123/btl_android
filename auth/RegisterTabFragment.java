package com.nhom5.healthtracking.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.AuthState;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RegisterTabFragment extends Fragment {

    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private RegisterTabViewModel mViewModel;
    private boolean isPerformingRegistration = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register_tab, container, false);

        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = root.findViewById(R.id.confirm_password_edit_text);
        termsCheckBox = root.findViewById(R.id.terms_checkbox);
        registerButton = root.findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> performRegistration());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterTabViewModel.class);
        observeViewModel();
    }

    private void observeViewModel() {
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            registerButton.setEnabled(!isLoading);
            registerButton.setText(isLoading ? "Registering..." : "Register");
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                mViewModel.clearError(); // <-- fixed typo here
            }
        });

        mViewModel.getAuthState().observe(getViewLifecycleOwner(), auth -> {
            if (auth.isAuthenticated() && isPerformingRegistration) {
                isPerformingRegistration = false;
                navigateAfterRegister((AuthState.Authenticated) auth);
            }
        });
    }

    private void performRegistration() {
        isPerformingRegistration = true;

        String email = emailEditText.getText() == null ? "" : emailEditText.getText().toString().trim();
        String password = passwordEditText.getText() == null ? "" : passwordEditText.getText().toString();
        String confirm = confirmPasswordEditText.getText() == null ? "" : confirmPasswordEditText.getText().toString();
        boolean terms = termsCheckBox.isChecked();

        mViewModel.registerUser(email, password, confirm, terms);
    }

    private void navigateAfterRegister(AuthState.Authenticated auth) {
        User user = auth.getProfile();

        if (user.hasCompletedOnboarding()) {
            startActivity(new Intent(getActivity(), MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            startActivity(new Intent(getActivity(), OnboardingActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }

        requireActivity().finish();
    }
}
