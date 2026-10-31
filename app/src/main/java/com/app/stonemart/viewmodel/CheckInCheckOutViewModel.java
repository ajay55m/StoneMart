package com.app.stonemart.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.stonemart.UserLocalStore;
import com.app.stonemart.core.Resource;
import com.app.stonemart.data.repository.AttendanceRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CheckInCheckOutViewModel - Encapsulates geofence tracking and punch submission state.
 */
public class CheckInCheckOutViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;

    private final MutableLiveData<Boolean> _isInsideGeofence = new MutableLiveData<>(true);
    public final LiveData<Boolean> isInsideGeofence = _isInsideGeofence;

    private final MutableLiveData<Float> _distanceMeters = new MutableLiveData<>(0f);
    public final LiveData<Float> distanceMeters = _distanceMeters;

    private final MutableLiveData<Boolean> _isCheckedIn = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCheckedIn = _isCheckedIn;

    private final MutableLiveData<Resource<String>> _punchResult = new MutableLiveData<>();
    public final LiveData<Resource<String>> punchResult = _punchResult;

    private double currentDeviceLat = 26.9124;
    private double currentDeviceLng = 75.7873;

    public CheckInCheckOutViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AttendanceRepository(application);
        _isCheckedIn.setValue(repository.getUserStore().isCheckedIn());
    }

    public AttendanceRepository getRepository() {
        return repository;
    }

    public UserLocalStore getUserStore() {
        return repository.getUserStore();
    }

    public String getEmpName() {
        return repository.getUserStore().getEmpName();
    }

    public String getCheckInTime() {
        return repository.getUserStore().getCheckInTime();
    }

    public void updateLocation(double lat, double lng, float distanceMeters, boolean inside) {
        this.currentDeviceLat = lat;
        this.currentDeviceLng = lng;
        _distanceMeters.setValue(distanceMeters);
        _isInsideGeofence.setValue(inside);
    }

    public void submitPunch() {
        boolean currentlyCheckedIn = repository.getUserStore().isCheckedIn();
        String action = !currentlyCheckedIn ? "CHECK_IN" : "CHECK_OUT";
        String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());

        _punchResult.setValue(Resource.loading());
        repository.submitPunch(action, currentTime, currentDeviceLat, currentDeviceLng, new AttendanceRepository.PunchCallback() {
            @Override
            public void onSuccess(String message, String timestamp, String act) {
                _isCheckedIn.setValue("CHECK_IN".equals(act));
                _punchResult.setValue(Resource.success(message + " at " + timestamp));
            }

            @Override
            public void onFallbackSaved(String message, String timestamp, String act) {
                _isCheckedIn.setValue("CHECK_IN".equals(act));
                _punchResult.setValue(Resource.success(message));
            }
        });
    }
}
