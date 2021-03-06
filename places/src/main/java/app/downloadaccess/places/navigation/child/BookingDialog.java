package app.downloadaccess.places.navigation.child;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import app.downloadaccess.places.R;
import app.downloadaccess.resources.Utils;
import app.downloadaccess.resources.models.Place;
import app.downloadaccess.resources.models.Slot;
import app.downloadaccess.resources.network.RetrofitClientInstance;
import app.downloadaccess.resources.network.RetrofitService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;

public class BookingDialog {
    private static final String TAG = BookingDialog.class.getSimpleName();
    private AppCompatActivity activity;
    private final RetrofitService retrofitService = RetrofitClientInstance.INSTANCE.buildService(RetrofitService.class);
    private SharedPreferences prefs;
    private Place place;
    private Slot slot;
    private String displayDate;
    private Calendar datePickerCal;
    private TextView pickerTV, startHourTV, endHourTV;
    private AlertDialog alertDialog;
    private OnBookingDialogDismissed callback;

    public BookingDialog(AppCompatActivity activity, Place place, Slot slot, OnBookingDialogDismissed callback) {
        this.prefs = activity.getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);
        this.activity = activity;
        this.place = place;
        this.slot = slot;
        this.callback = callback;
    }

    public void setupDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialog);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog, null, false);
        Calendar current = Calendar.getInstance(Locale.getDefault());
        datePickerCal = Calendar.getInstance(Locale.getDefault());
        displayDate = getDisplayDate(current);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button deleteButton = dialogView.findViewById(R.id.delete_button);
        if (slot != null) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                deleteSlot(slot.getId());
                alertDialog.dismiss();
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
        pickerTV = dialogView.findViewById(R.id.chooseDatePicker);
        startHourTV = dialogView.findViewById(R.id.startHourTV);
        endHourTV = dialogView.findViewById(R.id.endHourTV);
        pickerTV.setText(displayDate != null ? displayDate : "");
        MaterialDatePicker.Builder<Long> pickerBuilder = MaterialDatePicker.Builder.datePicker();
        MaterialDatePicker<Long> picker = pickerBuilder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            datePickerCal.setTimeInMillis(selection);
            displayDate = getDisplayDate(datePickerCal);
            pickerTV.setText(displayDate);
        });
        pickerTV.setOnClickListener(v -> {
            picker.show(activity.getSupportFragmentManager(), picker.toString());
        });

        startHourTV.setText(String.format("%02d:%02d", current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE)));
        startHourTV.setOnClickListener(v -> {
            MaterialTimePicker startHourPicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(CLOCK_24H)
                    .setHour(current.get(Calendar.HOUR_OF_DAY))
                    .setMinute(current.get(Calendar.MINUTE))
                    .build();
            startHourPicker.addOnPositiveButtonClickListener(view -> {
                startHourTV.setText(String.format("%02d:%02d", startHourPicker.getHour(), startHourPicker.getMinute()));
            });
            startHourPicker.show(activity.getSupportFragmentManager(), startHourPicker.toString());
        });

        endHourTV.setText(String.format("%02d:%02d", current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE)));
        endHourTV.setOnClickListener(v -> {
            MaterialTimePicker endHourPicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(CLOCK_24H)
                    .setHour(current.get(Calendar.HOUR_OF_DAY))
                    .setMinute(current.get(Calendar.MINUTE))
                    .build();
            endHourPicker.addOnPositiveButtonClickListener(view -> {
                endHourTV.setText(String.format("%02d:%02d", endHourPicker.getHour(), endHourPicker.getMinute()));
            });
            endHourPicker.show(activity.getSupportFragmentManager(), endHourPicker.toString());
        });

        MaterialButton standard_button = dialogView.findViewById(R.id.standard_button);
        MaterialButton priority_button = dialogView.findViewById(R.id.priority_button);
        priority_button.setOnClickListener(v -> {
            priority_button.setTextColor(ContextCompat.getColor(activity, R.color.white));
            priority_button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.colorAccent));
            standard_button.setTextColor(ContextCompat.getColor(activity, R.color.text_grey));
            standard_button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.disabled_tint));
        });
        standard_button.setOnClickListener(v -> {
            standard_button.setTextColor(ContextCompat.getColor(activity, R.color.white));
            standard_button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.colorPrimary));
            priority_button.setTextColor(ContextCompat.getColor(activity, R.color.text_grey));
            priority_button.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.disabled_tint));
        });

        builder.setView(dialogView);
        alertDialog = builder.create();
        EditText maxVisitors = dialogView.findViewById(R.id.visitorsCount);
        saveButton.setOnClickListener(v -> {
            if (maxVisitors.getText().toString().trim().isEmpty() || maxVisitors.getText().toString().trim().equals("0")) {
                new MaterialAlertDialogBuilder(activity).setTitle("Number of visitors required").setMessage("Enter number of visitors").show();
                return;
            }
            Slot slot = new Slot();
            slot.setType(standard_button.isChecked() ? "Standard" : "Priority");
            slot.setFrom(datePickerCal.get(Calendar.DAY_OF_MONTH) + "." + (datePickerCal.get(Calendar.MONTH) + 1) + "." + datePickerCal.get(Calendar.YEAR) +
                    " " + startHourTV.getText().toString());
            slot.setTo(datePickerCal.get(Calendar.DAY_OF_MONTH) + "." + (datePickerCal.get(Calendar.MONTH) + 1) + "." + datePickerCal.get(Calendar.YEAR) +
                    " " + endHourTV.getText().toString());
            slot.setMaxSlots(Integer.parseInt(maxVisitors.getText().toString()));
            addNewSlot(place.getId(), slot);
            alertDialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    public void addNewSlot(String placeId, Slot slot) {
        ProgressDialog pd = Utils.showLoadingIndicator(activity);
        slot.setUserId(prefs.getString("userId", null));
        retrofitService.addSlot(Utils.getJwtToken(activity), placeId, slot).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                pd.dismiss();
                callback.onDismiss();
                if (response.code() > 300 && response.errorBody() != null) {
                    try {
                        JsonObject errorObject = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                        Toast.makeText(activity, errorObject.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(activity, "Slot created!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                pd.dismiss();
                callback.onDismiss();
                Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSlot(String slotId) {
        retrofitService.deleteSlot(Utils.getJwtToken(activity), slotId, prefs.getString("userId", null)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                if (response.code() > 300 && response.errorBody() != null) {
                    try {
                        JsonObject errorObject = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                        Toast.makeText(activity, errorObject.get("message").getAsString(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDisplayDate(Calendar calendar) {
        if (calendar != null) {
            String date = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + ", " + calendar.get(Calendar.DAY_OF_MONTH) + Utils.getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH)) + " " + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            return date;
        }
        return null;
    }

    interface OnBookingDialogDismissed {
        void onDismiss();
    }

}
