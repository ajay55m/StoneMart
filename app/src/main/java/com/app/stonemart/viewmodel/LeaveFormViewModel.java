package com.app.stonemart.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.stonemart.LeaveRequestRecord;
import com.app.stonemart.LeaveTypeItem;
import com.app.stonemart.UserLocalStore;
import com.app.stonemart.core.Resource;
import com.app.stonemart.data.repository.LeaveRepository;

import java.util.List;

/**
 * LeaveFormViewModel - Manages leave application state, validation, and submission streams.
 */
public class LeaveFormViewModel extends AndroidViewModel {

    private final LeaveRepository repository;

    private final MutableLiveData<List<LeaveTypeItem>> _leaveTypes = new MutableLiveData<>();
    public final LiveData<List<LeaveTypeItem>> leaveTypes = _leaveTypes;

    private final MutableLiveData<LeaveTypeItem> _selectedLeaveType = new MutableLiveData<>();
    public final LiveData<LeaveTypeItem> selectedLeaveType = _selectedLeaveType;

    private final MutableLiveData<Boolean> _isHalfDay = new MutableLiveData<>(false);
    public final LiveData<Boolean> isHalfDay = _isHalfDay;

    private final MutableLiveData<String> _halfDaySession = new MutableLiveData<>("AM");
    public final LiveData<String> halfDaySession = _halfDaySession;

    private final MutableLiveData<Resource<LeaveRequestRecord>> _submitResult = new MutableLiveData<>();
    public final LiveData<Resource<LeaveRequestRecord>> submitResult = _submitResult;

    private final MutableLiveData<List<LeaveRequestRecord>> _recentLeaves = new MutableLiveData<>();
    public final LiveData<List<LeaveRequestRecord>> recentLeaves = _recentLeaves;

    public LeaveFormViewModel(@NonNull Application application) {
        super(application);
        this.repository = new LeaveRepository(application);
        loadLeaveTypes();
        loadRecentLeaves();
    }

    public LeaveRepository getRepository() {
        return repository;
    }

    public UserLocalStore getUserStore() {
        return repository.getUserStore();
    }

    public void loadLeaveTypes() {
        List<LeaveTypeItem> types = repository.getAvailableLeaveTypes();
        _leaveTypes.setValue(types);
        if (!types.isEmpty()) {
            _selectedLeaveType.setValue(types.get(0));
        }
    }

    public void selectLeaveType(LeaveTypeItem item) {
        _selectedLeaveType.setValue(item);
        if (item != null && !item.isHalfDayAllowed() && Boolean.TRUE.equals(_isHalfDay.getValue())) {
            _isHalfDay.setValue(false);
        }
    }

    public void setHalfDay(boolean halfDay) {
        LeaveTypeItem current = _selectedLeaveType.getValue();
        if (halfDay && current != null && !current.isHalfDayAllowed()) {
            return;
        }
        _isHalfDay.setValue(halfDay);
    }

    public void setHalfDaySession(String session) {
        _halfDaySession.setValue(session);
    }

    public void loadRecentLeaves() {
        _recentLeaves.setValue(repository.getRecentLeaves());
    }

    public void submitLeave(String fromDate, String toDate, String reason) {
        LeaveTypeItem currentType = _selectedLeaveType.getValue();
        if (currentType == null) {
            _submitResult.setValue(Resource.error("Please select a valid leave category."));
            return;
        }

        boolean halfDay = Boolean.TRUE.equals(_isHalfDay.getValue());
        if (halfDay) {
            toDate = fromDate;
            if (fromDate == null || fromDate.trim().isEmpty()) {
                _submitResult.setValue(Resource.error("Please select the leave date."));
                return;
            }
        } else {
            if (fromDate == null || fromDate.trim().isEmpty() || toDate == null || toDate.trim().isEmpty()) {
                _submitResult.setValue(Resource.error("Please select valid From and To dates."));
                return;
            }

            if (toDate.compareTo(fromDate) < 0) {
                _submitResult.setValue(Resource.error("End date cannot be earlier than start date."));
                return;
            }
        }

        if (reason == null || reason.trim().length() < 3) {
            _submitResult.setValue(Resource.error("Please enter your reason for leave (minimum 3 characters)."));
            return;
        }

        _submitResult.setValue(Resource.loading());
        repository.submitLeaveRequest(currentType, halfDay, fromDate, toDate, _halfDaySession.getValue(), reason.trim(), new LeaveRepository.LeaveSubmitCallback() {
            @Override
            public void onSuccess(LeaveRequestRecord record) {
                loadRecentLeaves();
                _submitResult.setValue(Resource.success(record));
            }

            @Override
            public void onFallback(LeaveRequestRecord fallbackRecord) {
                loadRecentLeaves();
                _submitResult.setValue(Resource.success(fallbackRecord));
            }

            @Override
            public void onError(String errorMessage) {
                _submitResult.setValue(Resource.error(errorMessage));
            }
        });
    }
}
