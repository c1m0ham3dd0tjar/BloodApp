 package com.example.licenta;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta.model.DonationCenter;
import com.example.licenta.model.Request;
import com.example.licenta.model.User;
import com.example.licenta.viewholder.RequestViewHolder;
import com.example.licenta.viewholder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestFragment extends Fragment {

    public static final String BLOOD_TYPE_0_MINUS = "0(I)-";
    public static final String BLOOD_TYPE_0_PLUS = "0(I)+";
    public static final String BLOOD_TYPE_A_MINUS = "A(II)-";
    public static final String BLOOD_TYPE_A_PLUS = "A(II)+";
    public static final String BLOOD_TYPE_B_MINUS = "B(III)-";
    public static final String BLOOD_TYPE_B_PLUS = "B(III)+";
    public static final String BLOOD_TYPE_AB_MINUS = "AB(IV)-";
    public static final String BLOOD_TYPE_AB_PLUS = "AB(IV)+";

    public static final String RH_POSITIVE = "Pozitiv";
    public static final String RH_NEGATIVE  = "Negativ";

    private int shortAnimationDuration;

    private DonationCenter donationCenter;

    SpinKitView spinner;
    TextView tvNoRequestsInfo;
    RecyclerView recyclerViewDonors;
    RecyclerView.LayoutManager layoutManager;
    ProgressBar progressBar;
    DatabaseReference myRequestsReference;
    FloatingActionButton fabCreateRequest;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Location currentUserLocation;
    private List<User> usersList = new ArrayList<>();

    RecyclerView recyclerViewMyRequests;
    String currentUserId;

    ConstraintLayout constraintLayoutDonors;
    ImageView btnShowMyRequests;

    private List<Request> myRequestsList = new ArrayList<>();
    private DisplayData dataAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request, container, false);

        tvNoRequestsInfo = view.findViewById(R.id.tvNoRequestsInfo);
        spinner = view.findViewById(R.id.spin_kit);

        constraintLayoutDonors = view.findViewById(R.id.constraintLayoutDonors);
        btnShowMyRequests = view.findViewById(R.id.btnShowMyRequests);

        donationCenter = new DonationCenter();

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Firebase init
        currentUserId = currentUser.getUid();
        myRequestsReference = FirebaseDatabase.getInstance().getReference("Requests");

//        // Set recycler view
//        recyclerView = view.findViewById(R.id.recyclerViewUsers);
//        recyclerView.setHasFixedSize(true);
//
//        if (getContext() != null)
//            layoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(layoutManager);

        recyclerViewMyRequests = view.findViewById(R.id.recyclerViewMyRequests);
        recyclerViewMyRequests.setHasFixedSize(true);
        if (getContext() != null)
            layoutManager = new LinearLayoutManager(getContext());
        recyclerViewMyRequests.setLayoutManager(layoutManager);

        recyclerViewDonors = view.findViewById(R.id.recyclerViewDonors);
        recyclerViewDonors.setHasFixedSize(true);
        if (getContext() != null)
            layoutManager = new LinearLayoutManager(getContext());
        recyclerViewDonors.setLayoutManager(layoutManager);


        progressBar = view.findViewById(R.id.progressBarRequest);
        fabCreateRequest = view.findViewById(R.id.fabCreateRequest);

        //displayListOfUsers();

        showListOfMyRequests();

        fabCreateRequest.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateRequestActivity.class));
            }
        });

        btnShowMyRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the content view to 0% opacity but visible, so that it is visible
                // (but fully transparent) during the animation.
                recyclerViewMyRequests.setAlpha(0f);
                recyclerViewMyRequests.setVisibility(View.VISIBLE);

                // Animate the content view to 100% opacity, and clear any animation listener set on the view.
                recyclerViewMyRequests.animate()
                        .alpha(1f)
                        .setDuration(shortAnimationDuration)
                        .setListener(null);

                // Animate the loading view to 0% opacity. After the animation ends,
                // set its visibility to GONE as an optimization step (it won't
                // participate in layout passes, etc.)
                constraintLayoutDonors.animate()
                        .alpha(0f)
                        .setDuration(shortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                constraintLayoutDonors.setVisibility(View.GONE);
                            }
                        });
            }
        });

        return view;
    }

    private void displayListOfUsers() {

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(myRequestsReference, User.class)
                .build();

        FirebaseRecyclerAdapter<User, UserViewHolder> adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull User user) {
                holder.tvUserName.setText(user.getName());

                progressBar.setVisibility(View.INVISIBLE);
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_users, parent, false);

                return new UserViewHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerViewDonors.setAdapter(adapter);

        // make multiple adapters, set adapter when pressing button or something
    }

    private void showListOfMyRequests() {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(myRequestsReference.orderByChild("requesterId").equalTo(currentUserId), Request.class)
                    .build();

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
                holder.patientAge = request.getPatientAge();
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

                /*holder.btnShare.setOnClickListener(v -> {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setQuote("Useful link.")
                            .setContentUrl(Uri.parse("https://google.com"))
                            .build();
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        shareDialog.show(linkContent);
                    }
                    // sharePhoto();
                });*/


                holder.btnDeleteRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
                        dialog.setPositiveButton(getString(R.string.string_confirmation), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Requests");
                                ref.child(request.getKey()).removeValue().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), getResources().getString(R.string.string_confirmed_data), Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), getResources().getString(R.string.string_error), Toast.LENGTH_SHORT).show();
                                            Log.e("DeleteRequestFail", e.toString());
                                        });

                            }
                        })
                                .setNegativeButton(getString(R.string.string_cancel), (dialog1, which) -> {

                                })
                                .setTitle(getString(R.string.string_delete_request_confirmation))
                                .show();
                    }
                });

                holder.btnSearchDonors.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recyclerViewMyRequests.setVisibility(View.GONE);

                        // Get Location retrieves the name of the donation center
                        showAvailableDonors(request.getBloodType(), request.getLocation());
                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_listitem_my_requests, parent, false);

                return new RequestViewHolder(view);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerViewMyRequests.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
    }

    private Location getRequestLocation(Request request) {

        String donationCenterName = request.getLocation();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");
        Log.d("allen", "after ref");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("allen", "OnDataChange");
                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d("allen", "for");
                    if (Objects.requireNonNull(data.child("name").getValue()).toString().equals(donationCenterName)) {
                        Log.d("allen", "if");
                        donationCenter = data.getValue(DonationCenter.class);
                        Log.d("allen", donationCenter.getPhone1());
                        Log.d("allen", "this is name " + donationCenterName + " this is from the database " + data.child("name").getValue().toString());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RequestFragment", error.toString());
            }
        });

        Log.d("allen", "outside ref" + donationCenter.getPhone1());

        Location location = new Location("");
        location.setLongitude(donationCenter.getLongitude());
        location.setLatitude(donationCenter.getLatitude());

        return location;
    }

    private void showAvailableDonors(String patientBloodType, String donationCenterName) {

        spinner.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);

                    Log.d("allen", "location: " + Objects.requireNonNull(data.child("Location").getValue()).toString());


                    assert user != null;

                    Log.d("allen", "crapa 1");
                    Location userLocation = new Location("");

                    userLocation.setLongitude((Double) data.child("Location/longitude").getValue());
                    userLocation.setLatitude((Double) data.child("Location/latitude").getValue());
                    Log.d("allen", "crapa 2");


                    Log.d("allen", "userLocation: " + userLocation.toString());

                    user.setLocation(userLocation);

                    Log.d("allen", "showAvailableDonors: user: " + user.toString());
                    if (user.isAvailable()) {
                        if (donorIsCompatible(user.getBloodType(), user.getRH(), patientBloodType))
                            usersList.add(user);
                    }

//                    usersList.sort(new Comparator<User>() {
//                        @Override
//                        public int compare(User o1, User o2) {
//                            return 0;
//                        }
//                    });
                }

                dataAdapter = new DisplayData(getActivity(), usersList, donationCenterName);

                dataAdapter.notifyDataSetChanged();
                recyclerViewDonors.setAdapter(dataAdapter);

                // Set the content view to 0% opacity but visible, so that it is visible
                // (but fully transparent) during the animation.
                constraintLayoutDonors.setAlpha(0f);
                constraintLayoutDonors.setVisibility(View.VISIBLE);

                // Animate the content view to 100% opacity, and clear any animation listener set on the view.
                constraintLayoutDonors.animate()
                        .alpha(1f)
                        .setDuration(shortAnimationDuration)
                        .setListener(null);

                // Animate the loading view to 0% opacity. After the animation ends,
                // set its visibility to GONE as an optimization step (it won't
                // participate in layout passes, etc.)
                recyclerViewMyRequests.animate()
                        .alpha(0f)
                        .setDuration(shortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                recyclerViewMyRequests.setVisibility(View.GONE);
                            }
                        });

                spinner.setVisibility(View.GONE);

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("loadingDonorsError", databaseError.toString());
            }
        });
    }

    private boolean donorIsCompatible(String donorBloodType, @NonNull String donorRh, String patientBloodType) {

        boolean isCompatible = true;

        String bloodType = "";
        if (donorRh.equals(RH_POSITIVE)) {
            bloodType = donorBloodType + "+";
        }
        if (donorRh.equals(RH_NEGATIVE)) {
            bloodType = donorBloodType + "-";
        }

        switch (patientBloodType) {

            case BLOOD_TYPE_0_MINUS:
                if (!donorBloodType.equals(BLOOD_TYPE_0_MINUS))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_0_PLUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_PLUS) || bloodType.equals(BLOOD_TYPE_0_MINUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_A_MINUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_MINUS) || bloodType.equals(BLOOD_TYPE_A_MINUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_A_PLUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_MINUS) || bloodType.equals(BLOOD_TYPE_A_MINUS)
                             || bloodType.equals(BLOOD_TYPE_0_PLUS) || bloodType.equals(BLOOD_TYPE_A_PLUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_B_MINUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_MINUS) || bloodType.equals(BLOOD_TYPE_B_MINUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_B_PLUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_MINUS) || bloodType.equals(BLOOD_TYPE_B_MINUS) ||
                        bloodType.equals(BLOOD_TYPE_0_PLUS) || bloodType.equals(BLOOD_TYPE_B_PLUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_AB_MINUS:
                if (!(bloodType.equals(BLOOD_TYPE_0_MINUS) || bloodType.equals(BLOOD_TYPE_A_MINUS) ||
                        bloodType.equals(BLOOD_TYPE_B_MINUS) || bloodType.equals(BLOOD_TYPE_AB_MINUS)))
                    isCompatible = false;
                break;

            case BLOOD_TYPE_AB_PLUS:
                isCompatible = true;
                break;
        }

        return isCompatible;
    }
}

class DisplayData extends RecyclerView.Adapter<UserViewHolder> implements Filterable {

    private List<User> originalList = new ArrayList<>();
    private List<User> list = new ArrayList<>();
    private Context context;
    private String donationCenterName;

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_users, parent, false);
        return new UserViewHolder(view);
    }

    // Sending here as parameter the location of the donation center where the request is assigned, so I can calculate distance between
    // donor and centre and display it in the recycler view.

    public DisplayData(Context context, List<User> usersList, String donationCenterName) {
        this.context = context;
        this.originalList = usersList;
        this.list = usersList;
        this.donationCenterName = donationCenterName;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = originalList.get(position);

        holder.tvUserName.setText(user.getName());
        holder.tvDonorPhoneNumber.setText(user.getPhone());

        holder.tvLastDonationConfirmedDate.setText(R.string.string_last_donation);
        if (user.getLastDonationConfirmed().equals("none"))
            holder.tvLastDonationConfirmedDate.append(" -");
        else
            holder.tvLastDonationConfirmedDate.append(" " + user.getLastDonationConfirmed());

        Location userLocation = user.getLocation();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (Objects.requireNonNull(data.child("name").getValue()).toString().equals(donationCenterName)) {
                        Location donationCenterLocation = new Location("");

                        donationCenterLocation.setLatitude((Double) data.child("latitude").getValue());
                        donationCenterLocation.setLongitude((Double) data.child("longitude").getValue());

                        double dist = (int) userLocation.distanceTo(donationCenterLocation) / 1000;

                        holder.tvDistance.setText(String.valueOf(dist) + "km");

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RequestFragment", error.toString());
            }
        });

        setBloodTypeImage(user.getBloodType(), user.getRH(), holder.imgDonorBloodType);



    }

    @Override
    public int getItemCount() {
        return originalList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.clear();
                list.addAll(ArrayList.class.cast(results.values));
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredResults = null;

                filteredResults = getFilteredResults();

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }

            protected List<User> getFilteredResults() {

                return null;
            }

        };
    }

    public void setBloodTypeImage(String bloodType, String rh, ImageView imgViewBloodType) {

        switch(bloodType) {
            case "0(I)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.opositive));
                else
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.onegative));
                break;

            case "A(II)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.apositive));
                else
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.anegative));
                break;

            case "B(III)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.bpositive));
                else
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.bnegative));
                break;

            case "AB(IV)":
                if (rh.equals("Pozitiv"))
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.abpositive));
                else
                    imgViewBloodType.setImageDrawable(context.getDrawable(R.drawable.abnegative));
                break;
        }
    }
}