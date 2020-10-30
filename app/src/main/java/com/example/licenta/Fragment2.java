package com.example.licenta;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.licenta.model.Request;
import com.example.licenta.viewholder.RequestViewHolder;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Fragment2 extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private RecyclerView recyclerViewRequests;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBarRequests;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String requesterName;

    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ImageButton btnShare;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {

        // Facebook share
        FacebookSdk.sdkInitialize(this.getContext());

        View fragment2View = inflater.inflate(R.layout.fragment2_layout, container, false);

        // Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        btnShare = fragment2View.findViewById(R.id.btnShareOnFacebook);

        // Call back for Facebook share result.
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(getContext(), "Worked!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Sort by functionality
        ImageButton btnSortRequestsBy = fragment2View.findViewById(R.id.btnSortRequestsBy);
        btnSortRequestsBy.setOnClickListener(v -> showPopupMenu());

        // This sets up a settings menu icon on the right side of the toolbar
        //setHasOptionsMenu(true);

        // Firebase init
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Requests");

        // Set recycler view
        recyclerViewRequests = fragment2View.findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setHasFixedSize(true);

        if (getContext() != null)
            layoutManager = new LinearLayoutManager(getContext());
        recyclerViewRequests.setLayoutManager(layoutManager);

        progressBarRequests = fragment2View.findViewById(R.id.progressBarRequests);

        // Get request from firebase and display them into the recycler.
        displayListOfRequests(false, "", "");

        return fragment2View;
    }

    public void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), getView());
        popupMenu.setOnMenuItemClickListener(Fragment2.this);
        popupMenu.inflate(R.menu.options_menu_requests);
        popupMenu.show();
    }

    // Display list of requests from the database.
    public void displayListOfRequests(boolean sortList, String sortBy, String sortingCriterion) {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(reference, Request.class)
                    .build();

        if (sortList) {
            options = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(reference.orderByChild(sortBy).equalTo(sortingCriterion), Request.class)
                    .build();
        }

        FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int i, @NonNull Request request) {
                holder.tvTitle.setText(request.getPatientName());
                //holder.tvDonationCenter.setText(request.getLocation());
                holder.unitsNeeded = Integer.toString(request.getBloodUnitsNeeded());
                holder.tvUnitsNeeded.setText(request.getBloodUnitsNeeded() + " " + getResources().getString(R.string.string_units));
                holder.bloodtype = request.getBloodType();
                holder.requesterId = request.getRequesterId();
                holder.requesterContactNumber = request.getContactPhone();
                holder.tvShowRequiredDate.setText(request.getRequiredUptoDate());

                holder.donationCenterName = request.getLocation();

                holder.requiredDate = request.getRequiredUptoDate();
                holder.patientAge  = request.getPatientAge();
                holder.compatibleTypes = request.getCompatibleBloodTypes().toString();

                List<String> compatibleBloodTypes = new ArrayList<>(request.getCompatibleBloodTypes());
                for (String bloodType : compatibleBloodTypes) {
                    holder.tvCompatibleBloodTypes.append(bloodType + "   ");
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), RequestDetails.class);

                        //ADD DATA TO OUR INTENT
                        intent.putExtra("Patient name", request.getPatientName());
                        intent.putExtra("donationCenterName", holder.donationCenterName);
                        intent.putExtra("Bloodtype", holder.bloodtype);
                        intent.putExtra("Units needed", holder.unitsNeeded);
                        intent.putExtra("Requester id", holder.requesterId);
                        intent.putExtra("ContactPhone", holder.requesterContactNumber);
                        intent.putExtra("patientAge", holder.patientAge);
                        intent.putExtra("requiredDate", holder.requiredDate);
                        intent.putExtra("compatibleTypes", holder.compatibleTypes);


                        //START DETAIL ACTIVITY
                        startActivity(intent);
                    }
                });

                holder.btnOpenRequestDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), RequestDetails.class);

                        //ADD DATA TO OUR INTENT
                        intent.putExtra("Patient name", request.getPatientName());
                        intent.putExtra("donationCenterName", holder.donationCenterName);
                        intent.putExtra("Bloodtype", holder.bloodtype);
                        intent.putExtra("Units needed", holder.unitsNeeded);
                        intent.putExtra("Requester id", holder.requesterId);
                        intent.putExtra("ContactPhone", holder.requesterContactNumber);
                        intent.putExtra("patientAge", request.getPatientAge());
                        intent.putExtra("requiredDate", request.getRequiredUptoDate());
                        intent.putExtra("compatibleTypes", holder.compatibleTypes);

                        //START DETAIL ACTIVITY
                        startActivity(intent);
                    }
                });

                holder.btnShare.setOnClickListener(v -> {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setQuote("Useful link.")
                            .setContentUrl(Uri.parse("https://google.com"))
                            .build();
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        shareDialog.show(linkContent);
                    }
                    // sharePhoto();
                });

                progressBarRequests.setVisibility(View.INVISIBLE);
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_listitem_requests, parent, false);

                return new RequestViewHolder(view);
            }

        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerViewRequests.setAdapter(adapter);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.bloodTypeOption:
                LayoutInflater inflater = LayoutInflater.from(getContext());
                final View view = inflater.inflate(R.layout.sample, null);
                MaterialCardView cardAP = view.findViewById(R.id.cardAP);

                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext());
                dialog.setTitle(getResources().getString(R.string.blood_type_hint))
                        .setView(view)
                        .setPositiveButton("Buton", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String bloodType = getBloodType(view);
                                if (!bloodType.equals("")) {
                                    // Sorting the list, sort by bloodType, show all requests that have "bloodType" type.
                                    displayListOfRequests(true, "bloodType", bloodType);
                                    Toast.makeText(getContext(), bloodType, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();

                return true;

            case R.id.showAllOption:
                displayListOfRequests(false, "", "");
                return true;

            case R.id.showMyRequestsOption:
                String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                displayListOfRequests(true, "requesterId", currentUserId);
                return true;

            default:
                return false;
        }
    }

    // Get the bloodtype chosen by the user from the alert dialog. Finds the focused view and reads the bloodtype from it.
    public String getBloodType(@NonNull View v) {

        final View focus = v.findFocus();
        String bloodType = "";

        if (focus != null) {
            switch (focus.getId()) {
                case R.id.card0P:
                    bloodType = "0(I)+";
                    break;
                case R.id.card0N:
                    bloodType = "0(I)-";
                    break;
                case R.id.cardAP:
                    bloodType = "A(II)+";
                    break;
                case R.id.cardAN:
                    bloodType = "A(II)-";
                    break;
                case R.id.cardBP:
                    bloodType = "B(III)+";
                    break;
                case R.id.cardBN:
                    bloodType = "B(III)-";
                    break;
                case R.id.cardABP:
                    bloodType = "AB(IV)+";
                    break;
                case R.id.cardABN:
                    bloodType = "AB(IV)-";
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + focus.getId());
            }
        }
        return bloodType;
    }

    // Share picture to Facebook.
    public void sharePhoto() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blood_request);
        SharePhoto sharePhoto = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption("This is a caption")
                .build();

        if (ShareDialog.canShow(SharePhotoContent.class)) {
            Log.d("Y", "yes");
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(sharePhoto)
                    .setShareHashtag(new ShareHashtag.Builder()
                            .setHashtag("#Hastagist")
                            .build())
                    .build();
            ShareDialog.show(this, content);
        }
    }

    // For Facebook share.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // I think this is to use with setHasOptionsMenu() method. When creating a settings menu on the right of the toolbar.

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.options_menu_requests, menu);
//
//    }
}


