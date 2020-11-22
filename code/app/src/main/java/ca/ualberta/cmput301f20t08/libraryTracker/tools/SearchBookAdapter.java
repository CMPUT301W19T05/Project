package ca.ualberta.cmput301f20t08.libraryTracker.tools;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.cmput301f20t08.libraryTracker.R;
import ca.ualberta.cmput301f20t08.libraryTracker.models.Book;
/**
 * SearchBookAdapter
 * Tools for handling search book from book list
 *
 */
public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<Book> bookList;
    private ArrayList<Book> filteredBook;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private String constraintString;


    public SearchBookAdapter(Context context, List<Book> books) {

        this.bookList = books;
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        constraintString = "";
        //filteredBook = books;
    }

    public void addBook(Book book) {
        if (bookList == null) {
            bookList = new ArrayList<>();
        }
        if (!contains(book)&&(Book.AVAILABLE.equals(book.getStatus())||Book.REQUESTED
                .equals(book.getStatus()))){
            bookList.add(0, book);
            getFilter().filter(constraintString);
            notifyItemInserted(0);
        }

    }
    public void clear(){

        bookList = new ArrayList<>();
        filteredBook = new ArrayList<>();
        notifyDataSetChanged();


    }

    public void changeBook(Book book) {

        changedAdd(book);
        int index = 0;
        for (Book it : filteredBook) {
            if (it.getBookId().equals(book.getBookId())) {
                filteredBook.set(index, book);
                notifyItemChanged(index);
                return;
            } else {
                index++;
            }
        }
    }

    private void changedAdd(Book book) {
        int index = 0;
        for (Book it : bookList) {
            if (book.getBookId().equals(it.getBookId())) {
                bookList.set(index, book);
                return;
            }
        }
    }

    public boolean contains(Book book) {

        for (Book it : bookList) {
            if (it.getBookId().equals(book.getBookId())) {
                return true;
            }
        }
        return false;
    }

    public void removeBook(Book book) {
        removeAdd(book);
        int index = 0;
        if (filteredBook!= null){
            for (Book it : filteredBook) {
                if (it.getBookId().equals(book.getBookId())) {
                    filteredBook.remove(index);
                    notifyItemRemoved(index);
                    return;
                } else {
                    index++;
                }
            }
        }

    }

    private void removeAdd(Book book) {
        int index = 0;
        if (bookList!= null){
            for (Book it : bookList) {
                if (it.getBookId().equals(book.getBookId())) {
                    bookList.remove(index);
                    return;
                } else {
                    index++;
                }
            }
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_book_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Book book = filteredBook.get(position);
        Glide.with(mContext).load(Uri.parse(book.getPhoto()))
                .into(viewHolder.cover);
        viewHolder.title.setText(book.getTitle());
        viewHolder.author.setText(book.getAuthor());
        viewHolder.owner.setText(book.getOwner().getUsername());

    }

    public Book getItem(int position) {
        return filteredBook.get(position);
    }

    @Override
    public int getItemCount() {
        return filteredBook == null ? 0 : filteredBook.size();
    }


    public void setClickListener(ItemClickListener itemClickListener) {

        this.mClickListener = itemClickListener;
    }



    public static boolean containsAllWords(String word, String ...keywords) {
        if (word ==null || word.isEmpty()){
            return false;
        }
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraintString = constraint.toString();
                String[] constraintList = constraintString.split("\\s+");

                if (constraintString.isEmpty()) {
                    filteredBook = new ArrayList<>();
                } else {
                    ArrayList<Book> FilteredList = new ArrayList<>();
                    for (Book it : bookList) {
                        if (containsAllWords(it.getAllDescription(),constraintList)){
                            FilteredList.add(it);
                        }
                    }
                    filteredBook = FilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredBook;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredBook = (ArrayList<Book>) results.values;
                notifyDataSetChanged();
                Log.d("filteredBook", "publishResults: "+ filteredBook.size());
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView cover;
        private TextView title;
        private TextView author;
        private TextView owner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.reach_res_cover);
            title = itemView.findViewById(R.id.reach_res_title);
            author = itemView.findViewById(R.id.reach_res_author);
            owner = itemView.findViewById(R.id.reach_res_owner);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}

