package pt.ulisboa.tecnico.cmov.project;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.project.databinding.FragmentBooksBinding;

import java.util.List;

public class ItemRecyclerForBooksFragment extends RecyclerView.Adapter<ItemRecyclerForBooksFragment.ViewItem> {

    private final List<Book> books;

    public ItemRecyclerForBooksFragment(List<Book> books) {
        this.books = books;
    }

    @NonNull
    @Override
    public ViewItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewItem(FragmentBooksBinding.
                inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewItem itemBook, int position) {
        itemBook.book = books.get(position);
        itemBook.bookID.setText(String.valueOf(position + 1));
        itemBook.bookTitle.setText(books.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewItem extends RecyclerView.ViewHolder {
        public final TextView bookID;
        public final TextView bookTitle;
        public Book book;

        public ViewItem(FragmentBooksBinding binding) {
            super(binding.getRoot());
            bookID = binding.itemNumber;
            bookTitle = binding.content;
        }
    }
}