package com.app.stonemart.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.stonemart.UserLocalStore;
import com.app.stonemart.core.Resource;
import com.app.stonemart.data.repository.AttendanceRepository;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * ManageAttendanceViewModel - Coordinates calendar attendance, holiday markers,
 * permission approvals, and metric pills through reactive LiveData streams.
 */
public class ManageAttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;

    private final MutableLiveData<Resource<JSONObject>> _monthlyData = new MutableLiveData<>();
    public final LiveData<Resource<JSONObject>> monthlyData = _monthlyData;

    private final MutableLiveData<Integer> _currentYear = new MutableLiveData<>(2026);
    public final LiveData<Integer> currentYear = _currentYear;

    private final MutableLiveData<Integer> _currentMonth = new MutableLiveData<>(Calendar.OCTOBER);
    public final LiveData<Integer> currentMonth = _currentMonth;

    private final MutableLiveData<Integer> _selectedDay = new MutableLiveData<>(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    public final LiveData<Integer> selectedDay = _selectedDay;

    public ManageAttendanceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AttendanceRepository(application);
    }

    public AttendanceRepository getRepository() {
        return repository;
    }

    public UserLocalStore getUserStore() {
        return repository.getUserStore();
    }

    public int getCurrentYearValue() {
        return _currentYear.getValue() != null ? _currentYear.getValue() : 2026;
    }

    public int getCurrentMonthValue() {
        return _currentMonth.getValue() != null ? _currentMonth.getValue() : Calendar.OCTOBER;
    }

    public int getSelectedDayValue() {
        return _selectedDay.getValue() != null ? _selectedDay.getValue() : Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public void setYearMonth(int year, int month) {
        _currentYear.setValue(year);
        _currentMonth.setValue(month);
        loadMonthlyAttendance(year, month);
    }

    public void setSelectedDay(int day) {
        _selectedDay.setValue(day);
    }

    public void loadMonthlyAttendance(int year, int month) {
        // Only set loading if there's no data currently
        if (_monthlyData.getValue() == null || _monthlyData.getValue().getData() == null) {
            _monthlyData.setValue(Resource.loading());
        }

        repository.getMonthlyAttendance(year, month, new AttendanceRepository.AttendanceDataCallback() {
            @Override
            public void onDataLoaded(JSONObject jsonResponse, boolean isFromCache) {
                _monthlyData.setValue(Resource.success(jsonResponse));
            }

            @Override
            public void onError(String errorMessage) {
                if (_monthlyData.getValue() == null || _monthlyData.getValue().getData() == null) {
                    _monthlyData.setValue(Resource.error(errorMessage));
                }
            }
        });
    }
}
