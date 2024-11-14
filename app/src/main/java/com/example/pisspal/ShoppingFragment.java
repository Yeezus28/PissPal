package com.example.pisspal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pisspal.databinding.FragmentShoppingBinding;

import java.util.ArrayList;

public class ShoppingFragment extends Fragment {
    private FragmentShoppingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShoppingBinding.inflate(inflater, container, false);

        initRecyclerView();
        return binding.getRoot();
    }

    private void initRecyclerView() {
        ArrayList<ShopDomain> items = new ArrayList<>();
        // Provide URLs for each item
        items.add(new ShopDomain("Toilet Roll", "item_1", 10, "https://www.amazon.sg/Pinse-Toilet-Plant-Based-Unbleached-Bathroom/dp/B0CXSYYC39"));
        items.add(new ShopDomain("Soap", "item_2", 5, "https://www.amazon.sg/Pinse-Toilet-Plant-Based-Unbleached-Bathroom/dp/B0CXSYYC39"));
        items.add(new ShopDomain("Hand Sanitizer","hand_sanitiser",10,"https://www.amazon.sg/s?k=hand+sanitiser&crid=390X67BBWQH8R&sprefix=hand+sanitiser%2Caps%2C307&ref=nb_sb_noss_1"));
        items.add(new ShopDomain("Toothbrush","toothbrush",10,"https://www.amazon.sg/s?k=toothbrush&crid=AMTHHUKNHI9L&sprefix=toothbrus%2Caps%2C387&ref=nb_sb_noss_2"));
        items.add(new ShopDomain("Toothpaste","toothpaste",15,"https://www.amazon.sg/s?k=toothpaste&crid=1W430CMY4MGVW&sprefix=toothpast%2Caps%2C325&ref=nb_sb_noss_2"));
        items.add(new ShopDomain("Male Deodorant", "m_deodorant", 20, "https://www.amazon.sg/Antiperspirant-Deodorant-protection-formulated-Moisturizer/dp/B00SD8IK6M?th=1"));
        items.add(new ShopDomain("Female Deodorant", "f_deodorant", 11, "https://www.amazon.sg/Secret-Antiperspirant-Deodorant-Balanced-Invisible/dp/B000XGKL8C?th=1"));
        items.add(new ShopDomain("Male Shampoo","m_shampoo",20,"https://www.amazon.com/s?k=male+shampoo&crid=29ZQ8MQ2R2MLC&sprefix=mshampoo%2Caps%2C817&ref=nb_sb_noss_2"));
        items.add(new ShopDomain("Female Shampoo","f_shampoo",20,"https://www.amazon.com/s?k=female+shampoo&crid=XOFJLPG4EKSO&sprefix=male+shampoo%2Caps%2C856&ref=nb_sb_noss_1"));
        items.add(new ShopDomain("Male Conditioner","m_conditioner",20,"https://www.amazon.sg/s?k=men+conditioner&crid=I5FBNIAZMR53&sprefix=men+cond%2Caps%2C286&ref=nb_sb_ss_ts-doa-p_1_8"));
        items.add(new ShopDomain("Female Conditioner","f_conditioner",20,"https://www.amazon.sg/s?k=female+conditioner&crid=15K37FXH6QXS6&sprefix=female+conditioner%2Caps%2C319&ref=nb_sb_noss_1"));
        items.add(new ShopDomain("Personal Trainer","sky",100,"https://www.instagram.com/sky____g/"));

        // Pass context to ShopAdapter
        ShopAdapter adapter = new ShopAdapter(getActivity(), items);
        binding.Toiletries.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.Toiletries.setAdapter(adapter);
    }
}
