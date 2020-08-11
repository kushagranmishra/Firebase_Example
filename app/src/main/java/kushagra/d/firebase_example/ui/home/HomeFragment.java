package kushagra.d.firebase_example.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import kushagra.d.firebase_example.R;
import kushagra.d.firebase_example.model.Animal;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    public static final String ANIMALS = "animals";
    private RecyclerView rvAnimals;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        rvAnimals = view.findViewById(R.id.rvAnimals);
        rvAnimals.setLayoutManager(new LinearLayoutManager(getActivity()));
        setAnimalList();
    }
    public void setAnimalList() {
        List<Animal> animalList = new ArrayList<>();
        db.collection(ANIMALS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (DocumentSnapshot document : documents) {
                    animalList.add(document.toObject(Animal.class));
                }
                AnimalAdapter adapter = new AnimalAdapter(getActivity(), R.layout.card_animal_layout, animalList);
                rvAnimals.setAdapter(adapter);
            } else {
                String error = task.getException().getMessage();
                animalList.add(new Animal("NO DATA FOUND", ""));
                AnimalAdapter adapter = new AnimalAdapter(getActivity(), R.layout.card_animal_layout, animalList);
                rvAnimals.setAdapter(adapter);

            }
        });
    }

    class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.Holder> {
        Context c;
        int layout;
        List<Animal> animals;
        LayoutInflater inflater;

        public AnimalAdapter(Context c, int layout, List<Animal> animals) {
            this.c = c;
            this.layout = layout;
            this.animals = animals;
            inflater = LayoutInflater.from(c);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = inflater.inflate(layout, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Animal animal = animals.get(position);
            holder.tvName.setText(animal.name);
            holder.imgEdit.setTag(animal);
            holder.imgDelete.setTag(animal);
        }

        @Override
        public int getItemCount() {
            return animals.size();
        }

        public class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView imgEdit, imgDelete;

            public Holder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                imgDelete = itemView.findViewById(R.id.imgDelete);
                imgEdit = itemView.findViewById(R.id.imgEdit);

                imgDelete.setOnClickListener(view -> {
                            Animal animal = (Animal) view.getTag();
                            db.collection(ANIMALS).whereEqualTo("name", animal.name).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                Toast.makeText(c, "data received", Toast.LENGTH_SHORT).show();
                            });
                        });
                imgEdit.setOnClickListener(view2 -> {
                    Animal animal=(Animal) view2.getTag();
                    HomeFragmentDirections.ActionNavHomeToNavGallery action = HomeFragmentDirections.actionNavHomeToNavGallery(animal.name,animal.description);
                    action.setDescription(animal.description);
                    action.setName(animal.name);
                    Navigation.findNavController(view2).navigate(action);
                });
            }
        }
    }
}