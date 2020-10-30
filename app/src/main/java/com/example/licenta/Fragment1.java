package com.example.licenta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.example.licenta.model.DonationCenter;
import com.example.licenta.viewholder.DonationCenterViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Fragment1 extends Fragment implements PopupMenu.OnMenuItemClickListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ProgressBar progressBar;

    FirebaseRecyclerAdapter<DonationCenter, DonationCenterViewHolder> adapter;
    DatabaseReference reference;
    DatabaseReference locationReference;
    SearchView searchView;
    String currentUserId;
    FirebaseUser currentUser;
    MaterialCardView card;

    static Location currentUserLocation;

    MaterialButton btnCreateAppointment;
    AppCompatImageButton btnSortBy;
    private List<DonationCenter> donationCentersList = new ArrayList<>();
    private DisplayAllData dataAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragment1View = inflater.inflate(R.layout.fragment1_layout, container, false);

        // Get the current user and the current user id
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        currentUserId = currentUser.getUid();

        // Find the current user's location
        currentUserLocation = new Location("");
        locationReference = FirebaseDatabase.getInstance().getReference("User/" + currentUserId);
        ValueEventListener locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot ds = snapshot.child("Location");

                double latitude = (double) ds.child("latitude").getValue();
                double longitude = (double) ds.child("longitude").getValue();

                currentUserLocation.setLatitude(latitude);
                currentUserLocation.setLongitude(longitude);

                Log.d("pula", currentUserLocation.getLatitude() + " latitudinea on create");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("currentUserLocationErr", error.toString());
            }
        };
        locationReference.addListenerForSingleValueEvent(locationListener);

        // UI assignments
        card = fragment1View.findViewById(R.id.parent_layout);
        btnCreateAppointment = fragment1View.findViewById(R.id.btnCreateAppointment);
        searchView = fragment1View.findViewById(R.id.searchView);
        progressBar = fragment1View.findViewById(R.id.progressBar);
        btnSortBy = fragment1View.findViewById(R.id.btnSortBy);

        // Donation center firebase reference
        reference = FirebaseDatabase.getInstance().getReference("DonationCenter");

        // Set the search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                //showSearchResults(newText);

                return false;
            }
        });

        // Open CreateAppointmentActivity
        btnCreateAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateAppointment.class));
            }
        });

        // Sort by functionality
        btnSortBy.setOnClickListener(v -> showPopup());

        // Set recycler view
        recyclerView = fragment1View.findViewById(R.id.recyclerViewDonationCenters);
        recyclerView.setHasFixedSize(true);
        if (getContext() != null)
            layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        showListOfDonationCenters();

        return fragment1View;
    }

    // Display popup menu
    public void showPopup() {
        PopupMenu popupMenu = new PopupMenu(getContext(), getView());
        popupMenu.setOnMenuItemClickListener(Fragment1.this);
        popupMenu.inflate(R.menu.sort_donation_centers_options_menu);
        popupMenu.show();
    }

    // Order by menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sortByName:
                progressBar.setVisibility(View.VISIBLE);
                showListOrderedBy("name");
                return true;
            case R.id.sortByLocation:
                progressBar.setVisibility(View.VISIBLE);
                showListOrderedBy("location");
                return true;

            case R.id.showAll:
                showListOfDonationCenters();
                return true;
            default:
                return false;
        }
    }

    // Load firebase data into recycler view
    private void showListOfDonationCenters() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                donationCentersList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    DonationCenter dc = data.getValue(DonationCenter.class);
                    donationCentersList.add(dc);
                }

                dataAdapter = new DisplayAllData(getActivity(), donationCentersList);

                dataAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(dataAdapter);


                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("loadingDonCentersError", databaseError.toString());
            }
        });
    }

//    private void showSearchResults(String seq) {
//
//        FirebaseRecyclerOptions<DonationCenter> options = new FirebaseRecyclerOptions.Builder<DonationCenter>()
//                .setQuery(reference, DonationCenter.class)
//                .build();
//
//        adapter = new FirebaseRecyclerAdapter<DonationCenter, DonationCenterViewHolder>(options) {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            protected void onBindViewHolder(@NonNull DonationCenterViewHolder holder, int i, @NonNull DonationCenter donationCenter) {
//
//                if (donationCenter.getName().toLowerCase().contains(seq)) {
//                    holder.tvName.setText(donationCenter.getName());
//                    String phoneNumbers = donationCenter.getPhone1() + " / " + donationCenter.getPhone2();
//                    holder.tvPhoneNumbers.setText(phoneNumbers);
//                    holder.tvAdress.setText(donationCenter.getAdress());
//                    holder.tvProgram.setText(donationCenter.getProgram());
//
//                    Location currentUserLocation = new Location("");
//
//                    locationReference = FirebaseDatabase.getInstance().getReference("User/" + currentUserId);
//
//                    ValueEventListener locationListener = new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            DataSnapshot ds = snapshot.child("Location");
//
//                            double latitude = (double) ds.child("latitude").getValue();
//                            double longitude = (double) ds.child("longitude").getValue();
//
//                            currentUserLocation.setLatitude(latitude);
//                            currentUserLocation.setLongitude(longitude);
//
//                            Location location = new Location("");
//                            location.setLatitude(donationCenter.getLatitude());
//                            location.setLongitude(donationCenter.getLongitude());
//                            double dist = currentUserLocation.distanceTo(location) / 1000;
//
//
//                            holder.tvShowDistance.setText(String.valueOf(dist));
//                        } // on data change
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.w(TAG, "loadLocation:onCancelled", error.toException());
//                        }
//                    }; //// value event
//
//                    locationReference.addValueEventListener(locationListener);
//
//                    progressBar.setVisibility(View.INVISIBLE);
//                }
//            }
//
//
//            @NonNull
//            @Override
//            public DonationCenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.layout_listitem, parent, false);
//
//                return new DonationCenterViewHolder(view);
//            }
//
//        };
//
//        adapter.startListening();
//        //adapter.notifyDataSetChanged();
//        recyclerView.setAdapter(adapter);
//
//    }


    private void showListOrderedBy(String option) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonationCenter");

            if (option.equals("name")) {

                ref.orderByChild(option).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        donationCentersList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            DonationCenter dc = data.getValue(DonationCenter.class);
                            donationCentersList.add(dc);
                        }

                        dataAdapter = new DisplayAllData(getActivity(), donationCentersList);
                        recyclerView.setAdapter(dataAdapter);
                        dataAdapter.notifyDataSetChanged();

                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("loadingDonCentersError", databaseError.toString());
                    }
                });
            }
            else if (option.equals("location")) {
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        donationCentersList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            DonationCenter dc = data.getValue(DonationCenter.class);
                            donationCentersList.add(dc);
                        }

                        dataAdapter = new DisplayAllData(getActivity(), donationCentersList);
                        dataAdapter.getFilter().filter("");

                        recyclerView.setAdapter(dataAdapter);
                        dataAdapter.notifyDataSetChanged();

                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("loadingDonCentersError", databaseError.toString());
                    }
                });
            }
    }

    public static boolean isOpen(String program) throws ParseException {
        boolean isOpen = true;

        String open = program.substring(0, 8);
        String close = program.substring(9, 17);

        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        LocalTime openTime = LocalTime.parse(open);
        LocalTime closingTime = LocalTime.parse(close);


        if (today.getDayOfWeek().getValue() == 6 || today.getDayOfWeek().getValue() == 7) {
            isOpen = false;
        } else isOpen = now.isAfter(openTime) && now.isBefore(closingTime);

        return isOpen;
    }
}

class DisplayAllData extends RecyclerView.Adapter<DonationCenterViewHolder> implements Filterable {
    private List<DonationCenter> originalList = new ArrayList<>();
    private List<DonationCenter> list = new ArrayList<>();
    private Context context;

    @NonNull
    @Override
    public DonationCenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new DonationCenterViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DonationCenterViewHolder holder, int position) {
        DonationCenter donationCenter = originalList.get(position);

        // Get the current user - donation center distance in km.
        Location donationCenterLocation = new Location("");
        donationCenterLocation.setLatitude(donationCenter.getLatitude());
        donationCenterLocation.setLongitude(donationCenter.getLongitude());
        int dist = (int)(Fragment1.currentUserLocation.distanceTo(donationCenterLocation)) / 1000;
        holder.tvShowDistance.setText(dist + "km");

        // !!!
        Log.d("Alin", dist + "al");
        Log.d("Alin", donationCenter.getLatitude() + " " + donationCenter.getLongitude());

        holder.tvName.setText(donationCenter.getName());
        String phoneNumbers = donationCenter.getPhone1() + " / " + donationCenter.getPhone2();
        holder.tvPhoneNumbers.setText(phoneNumbers);
        holder.tvAdress.setText(donationCenter.getAdress());
        holder.tvProgram.setText(donationCenter.getProgram());

        try {
            if (Fragment1.isOpen(donationCenter.getProgram())) {
                holder.tvShowState.setText(context.getResources().getString(R.string.open));
            } else {
                holder.tvShowState.setText(context.getResources().getString(R.string.closed));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvPhoneNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context.getApplicationContext());
                LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
                final View view = inflater.inflate(R.layout.phone_numbers, null);
                TextView tvPhone1, tvPhone2;
                tvPhone1 = view.findViewById(R.id.tvPhone1);
                tvPhone2 = view.findViewById(R.id.tvPhone2);
                tvPhone1.setText(donationCenter.getPhone1());
                tvPhone2.setText(donationCenter.getPhone2());

                dialog.setTitle(context.getResources().getString(R.string.string_call))
                        .setView(view)
                        .show();
            }
        });

        holder.tvShowDistance.setText(String.valueOf(dist));

        holder.btnCollapseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expendableLayout.getVisibility() == View.GONE) {
                    holder.expendableLayout.setVisibility(View.VISIBLE);
                    TransitionManager.beginDelayedTransition(holder.parentLayout, new AutoTransition());
                    // Change icon
                    holder.btnCollapseCard.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_less));
                } else {
                    holder.expendableLayout.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(holder.parentLayout, new AutoTransition());
                    // Change icon
                    holder.btnCollapseCard.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_more));
                }
            }
        });

        holder.imgAdress.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + donationCenter.getAdress());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getApplicationContext().getPackageManager()) != null) {
                context.startActivity(mapIntent);
            }

        });

        holder.imgNavigation.setOnClickListener(v -> {
            String mode = "&mode=d";
            navigateTo(donationCenter.getAdress(), mode);
        });

//        holder.imgNavigation.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
//                final View view = inflater.inflate(R.layout.navigation_options, null);
//
//                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context.getApplicationContext());
//                dialog.setView(view)
//                        .show();
//                view.findViewById(R.id.imgNavigationBike).setOnClickListener(v1 -> {
//                    String mode = "&mode=b"; // For biking.
//                    navigateTo(donationCenter.getAdress(), mode);
//                });
//
//                view.findViewById(R.id.imgNavigationCar).setOnClickListener(v12 -> {
//                    String mode = "&mode=d"; // For driving.
//                    navigateTo(donationCenter.getAdress(), mode);
//                });
//
//                view.findViewById(R.id.imgNavigationWalk).setOnClickListener(v13 -> {
//                    String mode = "&mode=w"; // For walking.
//                    navigateTo(donationCenter.getAdress(), mode);
//                });
//
//                return true;
//            }
//        });
   }

    public DisplayAllData(Context context, List<DonationCenter> donationCentersList) {
        this.context = context;
        this.originalList = donationCentersList;
        this.list = donationCentersList;
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
                List<DonationCenter> filteredResults = null;

                filteredResults = getFilteredResults();

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }

            protected List<DonationCenter> getFilteredResults() {
                HashMap<DonationCenter, Double> hMap = new HashMap<>();

                for (DonationCenter center : originalList) {

                    double longitude = center.getLongitude();
                    double latitude = center.getLatitude();

                    Location donationCenterLocation = new Location("");
                    donationCenterLocation.setLongitude(longitude);
                    donationCenterLocation.setLatitude(latitude);

                    double distanceToCenter = Fragment1.currentUserLocation.distanceTo(donationCenterLocation) / 1000;
                    Log.d("Alin", distanceToCenter + "aicisia e din filter");

                    hMap.put(center, distanceToCenter);
                }

                // Sort hMap
                Map<DonationCenter, Double> sortedMap = sortByValues(hMap);

                List<DonationCenter> results = new ArrayList<>(sortedMap.keySet());
                Log.d("Alin", sortedMap.values().toString() + "eee");

                return results;
            }

        };
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());

        // Defined custom comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public void navigateTo(String address, String mode) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address + mode);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getApplicationContext().getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }
}

// The old method with the firebase recycler adapter
 /* private void showListOfDonationCenters() {

    double dist = 1;
    FirebaseRecyclerOptions<DonationCenter> options = new FirebaseRecyclerOptions.Builder<DonationCenter>()
            .setQuery(reference, DonationCenter.class)
            .build();

    adapter = new FirebaseRecyclerAdapter<DonationCenter, DonationCenterViewHolder>(options) {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onBindViewHolder(@NonNull DonationCenterViewHolder holder, int i, @NonNull DonationCenter donationCenter) {

            Location location = new Location("");
            location.setLatitude(donationCenter.getLatitude());
            location.setLongitude(donationCenter.getLongitude());
            double dist = currentUserLocation.distanceTo(location) / 1000;
            Log.d("Alin", dist + "");

            holder.tvName.setText(donationCenter.getName());
            String phoneNumbers = donationCenter.getPhone1() + " / " + donationCenter.getPhone2();
            holder.tvPhoneNumbers.setText(phoneNumbers);
            holder.tvAdress.setText(donationCenter.getAdress());
            holder.tvProgram.setText(donationCenter.getProgram());


            try {
                if (isOpen(donationCenter.getProgram())) {
                    holder.tvShowState.setText(getResources().getString(R.string.open));
                } else {
                    holder.tvShowState.setText(getResources().getString(R.string.closed));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvPhoneNumbers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    final View view = inflater.inflate(R.layout.phone_numbers, null);
                    TextView tvPhone1, tvPhone2;
                    tvPhone1 = view.findViewById(R.id.tvPhone1);
                    tvPhone2 = view.findViewById(R.id.tvPhone2);
                    tvPhone1.setText(donationCenter.getPhone1());
                    tvPhone2.setText(donationCenter.getPhone2());

                    dialog.setTitle(getResources().getString(R.string.string_call))
                            .setView(view)
                            .show();
                }
            });

            holder.tvShowDistance.setText(String.valueOf(dist));

            holder.btnCollapseCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.expendableLayout.getVisibility() == View.GONE) {
                        holder.expendableLayout.setVisibility(View.VISIBLE);
                        TransitionManager.beginDelayedTransition(holder.parentLayout, new AutoTransition());
                        // Change icon
                        holder.btnCollapseCard.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less));
                    } else {
                        holder.expendableLayout.setVisibility(View.GONE);
                        TransitionManager.beginDelayedTransition(holder.parentLayout, new AutoTransition());
                        // Change icon
                        holder.btnCollapseCard.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more));
                    }
                }
            });

            holder.imgAdress.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:" + donationCenter.getAdress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }

            });

            holder.imgNavigation.setOnClickListener(v -> {
                String mode = "&mode=d";
                navigateTo(donationCenter.getAdress(), mode);
            });

            holder.imgNavigation.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    final View view = inflater.inflate(R.layout.navigation_options, null);

                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
                    dialog.setView(view)
                            .show();
                    view.findViewById(R.id.imgNavigationBike).setOnClickListener(v1 -> {
                        String mode = "&mode=b"; // For biking.
                        navigateTo(donationCenter.getAdress(), mode);
                    });

                    view.findViewById(R.id.imgNavigationCar).setOnClickListener(v12 -> {
                        String mode = "&mode=d"; // For driving.
                        navigateTo(donationCenter.getAdress(), mode);
                    });

                    view.findViewById(R.id.imgNavigationWalk).setOnClickListener(v13 -> {
                        String mode = "&mode=w"; // For walking.
                        navigateTo(donationCenter.getAdress(), mode);
                    });

                    return true;
                }
            });


            progressBar.setVisibility(View.INVISIBLE);
        }

        @NonNull
        @Override
        public DonationCenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_listitem, parent, false);

            return new DonationCenterViewHolder(view);
        }

    };

    adapter.startListening();
    adapter.notifyDataSetChanged();
    recyclerView.setAdapter(adapter);
} */


