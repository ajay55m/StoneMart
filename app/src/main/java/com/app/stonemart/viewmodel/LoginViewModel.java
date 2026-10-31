package com.app.stonemart.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.stonemart.core.Resource;
import com.app.stonemart.data.repository.AuthRepository;

/**
 * LoginViewModel - Handles login authentication business logic and exposes LiveData states.
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<String>> _loginState = new MutableLiveData<>();
    public final LiveData<Resource<String>> loginState = _loginState;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            _loginState.setValue(Resource.error("Please enter both Username and Password."));
            return;
        }

        _loginState.setValue(Resource.loading());
        authRepository.login(username.trim(), password.trim(), new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String formattedName, String userRole) {
                _loginState.setValue(Resource.success(formattedName));
            }

            @Override
            public void onError(String errorMessage) {
                _loginState.setValue(Resource.error(errorMessage));
            }
        });
    }
}

