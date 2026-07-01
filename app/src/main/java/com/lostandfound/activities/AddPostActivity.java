package com.lostandfound.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lostandfound.R;
import com.lostandfound.databinding.ActivityAddPostBinding;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;
import com.lostandfound.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Form screen for reporting a Lost or Found item.
 */
public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding      binding;
    private FirebaseHelper              firebase;
    private FusedLocationProviderClient fusedLocation;

    private Uri    selectedImageUri  = null;
    private File   cameraImageFile   = null;
    private String selectedStatus    = Constants.STATUS_LOST;
    private String selectedCategory  = "";
    private String selectedDate      = "";
    private double selectedLatitude  = 0.0;
    private double selectedLongitude = 0.0;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                try {
                                    getContentResolver().takePersistableUriPermission(
                                            selectedImageUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );
                                } catch (Exception ignored) {
                                    // URI permission may already be persistable or not supported
                                }
                            }
                            showImagePreview(selectedImageUri);
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && cameraImageFile != null) {
                            selectedImageUri = ImageUtils.getUriForFile(AddPostActivity.this, cameraImageFile);
                            showImagePreview(selectedImageUri);
                        }
                    });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = true;
                        for (boolean granted : permissions.values()) {
                            if (!granted) { allGranted = false; break; }
                        }
                        if (allGranted) showImageSourceDialog();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebase      = FirebaseHelper.getInstance();
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        setupToolbar();
        setupStatusToggle();
        setupCategoryDropdown();
        setupDatePicker();
        setupLocationField();
        setupImageUploadCard();
        setupPostButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusToggle() {
        binding.btnStatusLost.setChecked(true);
        binding.toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnStatusLost) {
                    selectedStatus = Constants.STATUS_LOST;
                } else if (checkedId == R.id.btnStatusFound) {
                    selectedStatus = Constants.STATUS_FOUND;
                }
            }
        });
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.CATEGORIES);

        binding.actvCategory.setAdapter(adapter);
        binding.actvCategory.setOnItemClickListener((parent, view, pos, id) ->
                selectedCategory = Constants.CATEGORIES[pos]);
    }

    private void setupDatePicker() {
        View.OnClickListener dateClickListener = v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                selectedDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        .format(selected.getTime());
                binding.etDate.setText(selectedDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        binding.etDate.setOnClickListener(dateClickListener);
        binding.tilDate.setEndIconOnClickListener(dateClickListener);
    }

    @SuppressLint("MissingPermission")
    private void setupLocationField() {
        binding.tilLocation.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
                return;
            }
            binding.tilLocation.setHelperText("Detecting location…");
            fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            selectedLatitude  = location.getLatitude();
                            selectedLongitude = location.getLongitude();
                            reverseGeocode(location.getLatitude(), location.getLongitude());
                        } else {
                            binding.tilLocation.setHelperText("Could not get location. Type manually.");
                        }
                    })
                    .addOnFailureListener(e ->
                            binding.tilLocation.setHelperText("Location unavailable. Type manually."));
        });
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder =
                        new android.location.Geocoder(this, Locale.getDefault());
                java.util.List<android.location.Address> addresses =
                        geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    String locality = address.getLocality() != null ? address.getLocality() : "";
                    String subLocality = address.getSubLocality() != null ? address.getSubLocality() : "";
                    String readable = TextUtils.isEmpty(subLocality)
                            ? locality
                            : subLocality + ", " + locality;
                    runOnUiThread(() -> {
                        binding.etLocation.setText(readable);
                        binding.tilLocation.setHelperText("Location detected");
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() ->
                        binding.tilLocation.setHelperText("Geocoding failed. Type address manually."));
            }
        }).start();
    }

    private void setupImageUploadCard() {
        binding.cardImageUpload.setOnClickListener(v -> {
            boolean hasCameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            boolean hasStoragePermission = android.os.Build.VERSION.SDK_INT >= 33
                    ? ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    : ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (hasCameraPermission && hasStoragePermission) {
                showImageSourceDialog();
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    permissionLauncher.launch(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES
                    });
                } else {
                    permissionLauncher.launch(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    });
                }
            }
        });
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select image source")
                .setItems(new String[]{"Choose from Gallery", "Take a Photo"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else            openCamera();
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                cameraImageFile = ImageUtils.createTempImageFile(this);
                Uri photoUri = ImageUtils.getUriForFile(this, cameraImageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(cameraIntent);
            } catch (IOException e) {
                Snackbar.make(binding.getRoot(),
                        "Camera setup failed", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePreview(Uri imageUri) {
        binding.llUploadPlaceholder.setVisibility(View.GONE);
        binding.ivPreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.ivPreview);
    }

    private void setupPostButton() {
        binding.btnPost.setOnClickListener(v -> {
            if (validateForm()) uploadImageAndPost();
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String title       = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String location    = binding.etLocation.getText().toString().trim();

        if (selectedImageUri == null) {
            Snackbar.make(binding.getRoot(),
                    "Please select an image", Snackbar.LENGTH_SHORT).show();
            valid = false;
        }
        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError(getString(R.string.error_empty_field));
            valid = false;
        } else {
            binding.tilTitle.setError(null);
        }
        if (TextUtils.isEmpty(description)) {
            binding.tilDescription.setError(getString(R.string.error_empty_field));
            valid = false;
        } else {
            binding.tilDescription.setError(null);
        }
        if (TextUtils.isEmpty(selectedCategory)) {
            binding.tilCategory.setError("Please select a category");
            valid = false;
        } else {
            binding.tilCategory.setError(null);
        }
        if (TextUtils.isEmpty(selectedDate)) {
            binding.tilDate.setError("Please select the date it was spotted");
            valid = false;
        } else {
            binding.tilDate.setError(null);
        }
        if (TextUtils.isEmpty(location)) {
            binding.tilLocation.setError("Please enter or detect the location");
            valid = false;
        } else {
            binding.tilLocation.setError(null);
        }

        return valid;
    }

    private void uploadImageAndPost() {
        setPosting(true);

        String uid       = firebase.getCurrentUserId();
        String timestamp = String.valueOf(System.currentTimeMillis());
        StorageReference imageRef = firebase.getItemImageRef(uid, timestamp + ".jpg");

        UploadTask uploadTask = imageRef.putFile(selectedImageUri);

        uploadTask
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred())
                            / snapshot.getTotalByteCount();
                    int percent = (int) progress;
                    binding.tvUploadPercent.setText(percent + "%");
                    binding.llUploadProgress.setVisibility(View.VISIBLE);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> savePostToFirestore(downloadUri.toString()))
                .addOnFailureListener(e -> {
                    setPosting(false);
                    binding.llUploadProgress.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(),
                            getString(R.string.error_image_upload),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    private void savePostToFirestore(String imageUrl) {
        String uid      = firebase.getCurrentUserId();
        String title    = binding.etTitle.getText().toString().trim();
        String desc     = binding.etDescription.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();

        firebase.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String posterName   = userDoc.getString("name");
                    String posterAvatar = userDoc.getString("profilePictureUrl");
                    if (posterName == null)   posterName   = "Anonymous";
                    if (posterAvatar == null) posterAvatar = "";

                    Item item = new Item(
                            uid, title, desc, selectedCategory, selectedStatus,
                            imageUrl, selectedDate, location,
                            selectedLatitude, selectedLongitude,
                            posterName, posterAvatar
                    );

                    firebase.getDb()
                            .collection(Constants.COLLECTION_ITEMS)
                            .add(item)
                            .addOnSuccessListener(docRef -> {
                                setPosting(false);
                                Snackbar.make(binding.getRoot(),
                                        "Post published! 🎉",
                                        Snackbar.LENGTH_SHORT).show();
                                binding.getRoot().postDelayed(this::finish, 800);
                            })
                            .addOnFailureListener(e -> {
                                setPosting(false);
                                Snackbar.make(binding.getRoot(),
                                        getString(R.string.error_post_failed),
                                        Snackbar.LENGTH_LONG).show();
                            });
                });
    }

    private void setPosting(boolean posting) {
        binding.btnPost.setEnabled(!posting);
        binding.btnPost.setText(posting
                ? getString(R.string.saving_post)
                : getString(R.string.post_item));
        if (!posting) binding.llUploadProgress.setVisibility(View.GONE);
    }
}
