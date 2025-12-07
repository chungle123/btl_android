package com.nhom5.healthtracking.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class LoginTabFragment extends Fragment {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private LoginTabViewModel mViewModel;
    private boolean isPerformingLogin = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);

        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        loginButton = root.findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> performLogin());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginTabViewModel.class);
        observeViewModel();
    }

    private void observeViewModel() {

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loginButton.setEnabled(!isLoading);
            loginButton.setText(isLoading ? "Logging in..." : "Login");
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });

        mViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.isAuthenticated() && isPerformingLogin) {
                isPerformingLogin = false;
                navigateAfterLogin((AuthState.Authenticated) authState);
            }
        });
    }

    private void performLogin() {
        isPerformingLogin = true;

        String email = emailEditText.getText() == null ? "" : emailEditText.getText().toString().trim();
        String password = passwordEditText.getText() == null ? "" : passwordEditText.getText().toString();

        mViewModel.loginUser(email, password);
    }

    private void navigateAfterLogin(AuthState.Authenticated auth) {
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
