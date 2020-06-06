package dahoum.wales.access_app.navigation.child;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import dahoum.wales.access_app.R;
import dahoum.wales.access_app.models.Place;

public class VisitInfoFragment extends Fragment {

    private ImageView goBackButton;
    private FragmentCallback callback;

    private TextView placeName;
    private Place place;
    private ImageView image;


    public VisitInfoFragment() {
        // Required empty public constructor
    }

    public static VisitInfoFragment newInstance(Place place) {
        VisitInfoFragment fragment = new VisitInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("place", place);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(FragmentCallback callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitors_information, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        goBackButton = view.findViewById(R.id.goBack);
        goBackButton.setOnClickListener(v -> {
            getParentFragment().getChildFragmentManager().popBackStack();
        });

        place = (Place) getArguments().getSerializable("place");
        setupPlace(view);
    }

    private void setupPlace(View view) {
        placeName = view.findViewById(R.id.placeName);
        placeName.setText(place.getName());
        image = view.findViewById(R.id.image);
        if (place.getImage() != null) {
            byte[] decodedString = Base64.decode(place.getImage(), Base64.DEFAULT);
            Bitmap base64Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(base64Bitmap);
        }

    }
}