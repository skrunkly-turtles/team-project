package app;
import dataAccess.LibraryApiDataAccessObject;
import entity.Book;
import views.LookUpBookState;
import views.LookUpBookView;
import views.LookUpBookViewModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LibraryApiDataAccessObject dao = new LibraryApiDataAccessObject();
        List<Book> books = dao.searchByTitle("the lord of the rings");
        System.out.println(books);

        List<String> booksString = new ArrayList<>();
        for (Book book : books) {
            booksString.add(book.toString());
        }

        LookUpBookViewModel viewModel = new LookUpBookViewModel();
        viewModel.setState(new LookUpBookState(booksString, ""));
        LookUpBookView view = new LookUpBookView(viewModel);

        JFrame frame = new JFrame("Library");
        frame.setContentPane(view);                       // put the view in the window
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
