package tuv.lib.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tuv.lib.models.*;
import tuv.lib.models.interfaces.BookService;
import tuv.lib.models.interfaces.RentService;
import tuv.lib.models.interfaces.UserService;

import javax.swing.table.TableColumn;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.io.IOException;

/**Controller for the operator view 
 * Contains the main logic of the application
 * @author Daniela
 *
 */
public class OperatorController implements Initializable {
	private UserService userService;
	private BookService bookService;
	private RentService rentService;
	private Map<Button, Pane> panes;
	private Operator operator;
	private NotificationsTask nt;
	
	private final long TIMER_PERIOD = 10000;
	private static Timer timer  = null;
	
	public static Timer getTimer() {
		return timer;
	}

	public OperatorController(User u) {
		this.operator = new Operator(u);
	}

	@FXML
	private Label l_name;

	@FXML
	private Button btn_addBook, btn_removeBook, btn_addClient, btn_makeRent, btn_findBook, btn_findClient,
			btn_Classification, btn_getNotf;
	@FXML
	private Pane pln_addBook, pln_removeBook, pln_addClient, pln_makeRent, pln_findBook, pln_findClient,
			pln_Classification;

	/**
	 * Changes the front pane according to the chosen button
	 * 
	 * @param event
	 *            chosen button
	 */
	@FXML
	private void buttonAction(ActionEvent event) {
		for (Map.Entry<Button, Pane> entry : panes.entrySet()) {
			if (event.getSource() == entry.getKey()) {
				entry.getValue().setVisible(true);
			} else {
				entry.getValue().setVisible(false);
			}
		}
	}

	// @Override
	public void initialize(URL location, ResourceBundle resources) {
		initializePanes();
		this.userService = new UserServiceImpl();
		this.bookService = new BookServiceImpl();
		this.rentService = new RentServiceImpl();
		
		this.timer = new Timer();
		nt = new NotificationsTask(btn_getNotf);
		timer.scheduleAtFixedRate(nt,TIMER_PERIOD, TIMER_PERIOD);

		l_name.setText("Operator : " + this.operator.getName());

		for (Map.Entry<Button, Pane> entry : panes.entrySet())
			entry.getValue().setVisible(false);

		cb_findBook.setValue("Book Name");
		cb_findBook.setItems(find_by);

		tb_findBook_name.setEditable(true);
		tb_findBook_name.setDisable(false);

		tb_findBook_author.setEditable(false);
		tb_findBook_author.setDisable(true);

		tb_findBook_genre.setEditable(false);
		tb_findBook_genre.setDisable(true);

		tb_findBook_condition.setEditable(false);
		tb_findBook_condition.setDisable(true);

		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (cb_findBook.getValue() == "Book Name") {
					tb_findBook_name.setEditable(true);
					tb_findBook_name.setDisable(false);

					tb_findBook_author.setEditable(false);
					tb_findBook_author.setDisable(true);

					tb_findBook_genre.setEditable(false);
					tb_findBook_genre.setDisable(true);

					tb_findBook_condition.setEditable(false);
					tb_findBook_condition.setDisable(true);
				}

				else if (cb_findBook.getValue() == "Book Author") {
					tb_findBook_name.setEditable(false);
					tb_findBook_name.setDisable(true);

					tb_findBook_author.setEditable(true);
					tb_findBook_author.setDisable(false);

					tb_findBook_genre.setEditable(false);
					tb_findBook_genre.setDisable(true);

					tb_findBook_condition.setEditable(false);
					tb_findBook_condition.setDisable(true);
				}

				else if (cb_findBook.getValue() == "Book Genre") {
					tb_findBook_name.setEditable(false);
					tb_findBook_name.setDisable(true);

					tb_findBook_author.setEditable(false);
					tb_findBook_author.setDisable(true);

					tb_findBook_genre.setEditable(true);
					tb_findBook_genre.setDisable(false);

					tb_findBook_condition.setEditable(false);
					tb_findBook_condition.setDisable(true);

				}

				else if (cb_findBook.getValue() == "Condition") {
					tb_findBook_name.setEditable(false);
					tb_findBook_name.setDisable(true);

					tb_findBook_author.setEditable(false);
					tb_findBook_author.setDisable(true);

					tb_findBook_genre.setEditable(false);
					tb_findBook_genre.setDisable(true);

					tb_findBook_condition.setEditable(true);
					tb_findBook_condition.setDisable(false);
				}

			}
		};

		// Set on action
		cb_findBook.setOnAction(event);
	}

	/**
	 * Initialize the connection between the buttons and the panes
	 */
	private void initializePanes() {
		panes = new HashMap<Button, Pane>();
		panes.put(btn_addBook, pln_addBook);
		panes.put(btn_removeBook, pln_removeBook);
		panes.put(btn_addClient, pln_addClient);
		panes.put(btn_makeRent, pln_makeRent);
		panes.put(btn_findBook, pln_findBook);
		panes.put(btn_findClient, pln_findClient);
		panes.put(btn_Classification, pln_Classification);
	}

	// add book block
	@FXML
	private TextField tb_addBook_name, tb_addBook_author, tb_addBook_genre, tb_addBook_number;

	/**
	 * Added book to the library database
	 * 
	 * @param event
	 */
	@FXML
	private void addBook(ActionEvent event) {
		String name = tb_addBook_name.getText();
		List<String> authors = Arrays.asList(tb_addBook_author.getText().split(", "));
		String genre = tb_addBook_genre.getText();
		String number = tb_addBook_number.getText();

		boolean status = true;
		status &= Validator.hasCharsOnly(name);
		status &= Validator.hasCharsOnly(genre);
		status &= Validator.hasDigitsOnly(number);
		for (int i = 0; i < authors.size(); i++) {
			status &= Validator.hasCharsOnly(authors.get(i));
		}

		if (!status) {
			Validator.showWrongInputAllert();
			tb_addBook_name.clear();
			tb_addBook_author.clear();
			tb_addBook_genre.clear();
			tb_addBook_number.clear();
			return;
		}

		Book book = new Book(name, authors, genre, number);

		bookService.addBook(book);

		tb_addBook_name.clear();
		tb_addBook_author.clear();
		tb_addBook_genre.clear();
		tb_addBook_number.clear();

	}

	// remove book block
	@FXML
	private TextField tb_removeBook_name, tb_removeBook_number;

	/**Remove book entity by name and condition
	 * @param event
	 */
	@FXML
	private void removeBook(ActionEvent event) {
		String name = tb_removeBook_name.getText();
		int cond = Integer.parseInt(tb_removeBook_number.getText());

		boolean status = true;
		status &= Validator.hasCharsOnly(name);
		status &= Validator.hasDigitsOnly(tb_removeBook_number.getText());

		if (!status) {
			Validator.showWrongInputAllert();
			tb_removeBook_name.clear();
			tb_removeBook_number.clear();
			return;
		}

		Book b = new Book(name, cond);

		this.bookService.removeBook(b);

		tb_removeBook_name.clear();
		tb_removeBook_number.clear();
	}

	// find client block
	@FXML
	private TextField tb_findClient_name;
	@FXML
	private TableView tw_findClient;
	@FXML
	public javafx.scene.control.TableColumn tc_findClient_name, tc_findClient_phone, tc_findClient_recDate,
			tc_findClient_loyalty;

	/**Finds client by name 
	 * @param event
	 */
	@FXML
	private void findClient(ActionEvent event) {
		String name = tb_findClient_name.getText();

		boolean status = true;
		status &= Validator.hasCharsOnly(name);

		if (!status) {
			Validator.showWrongInputAllert();

			tb_findClient_name.clear();
			return;
		}

		List<Client> res = userService.findClients(name);

		tw_findClient.getItems().clear();
		
		tc_findClient_name.setCellValueFactory(new PropertyValueFactory<Object, Object>("name"));
		tc_findClient_phone.setCellValueFactory(new PropertyValueFactory<Object, Object>("phoneNum"));
		tc_findClient_loyalty.setCellValueFactory(new PropertyValueFactory<Object, Object>("loyalty"));
		tc_findClient_recDate.setCellValueFactory(new PropertyValueFactory<Object, Object>("recordDate"));

		for (int i = 0; i < res.size(); i++) {
			tw_findClient.getItems().add(res.get(i));
		}

	}

	ObservableList<String> find_by = FXCollections.observableArrayList("Book Name", "Book Author", "Book Genre",
			"Condition");
	@FXML
	private TextField tb_findBook_name, tb_findBook_author, tb_findBook_genre, tb_findBook_condition;
	@FXML
	private ComboBox cb_findBook;
	@FXML
	private TableView tw_findBook;
	@FXML
	public javafx.scene.control.TableColumn tc_findBook_name, tc_findBook_author, tc_findBook_genre, tc_findBook_invNum,
			tc_findBook_condition;

	/**Displays Books in the table view
	 * @param books
	 */
	private void displayBooks(List<Book> books) {
		tw_findBook.getItems().clear();
		if (books.isEmpty() || books == null) {
			tb_findBook_name.clear();
			tb_findBook_author.clear();
			tb_findBook_genre.clear();
			tb_findBook_condition.clear();

			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("No Records");
			alert.setHeaderText("No records found !!");
			alert.showAndWait();
			return;
		}

		tc_findBook_name.setCellValueFactory(new PropertyValueFactory<Object, Object>("Name"));
		tc_findBook_author.setCellValueFactory(new PropertyValueFactory<Object, Object>("Authors"));
		tc_findBook_genre.setCellValueFactory(new PropertyValueFactory<Object, Object>("Genre"));
		tc_findBook_invNum.setCellValueFactory(new PropertyValueFactory<Object, Object>("invNumber"));
		tc_findBook_condition.setCellValueFactory(new PropertyValueFactory<Object, Object>("Condition"));

		for (Iterator iterator = books.iterator(); iterator.hasNext();) {
			tw_findBook.getItems().add(iterator.next());
		}
	}

	/**Finds book by criteria
	 * @param event
	 */
	@FXML
	private void findBook(ActionEvent event) {

		if (cb_findBook.getValue() == "Book Name") {
			String name = tb_findBook_name.getText();
			if (!Validator.hasCharsOnly(name)) {
				Validator.showWrongInputAllert();
				tb_findBook_name.clear();
				return;
			}

			tb_findBook_author.clear();
			tb_findBook_genre.clear();
			tb_findBook_condition.clear();

			this.displayBooks(bookService.getBookbyName(name));
		} else if (cb_findBook.getValue() == "Book Author") {
			String author = tb_findBook_author.getText();
			if (!Validator.hasCharsOnly(author)) {
				Validator.showWrongInputAllert();
				tb_findBook_author.clear();
				return;
			}

			tb_findBook_name.clear();
			tb_findBook_genre.clear();
			tb_findBook_condition.clear();

			this.displayBooks(bookService.getBooksByAuthor(author));
		} else if (cb_findBook.getValue() == "Book Genre") {
			String genre = tb_findBook_genre.getText();
			if (!Validator.hasCharsOnly(genre)) {
				Validator.showWrongInputAllert();
				tb_findBook_genre.clear();
				return;
			}

			tb_findBook_name.clear();
			tb_findBook_author.clear();
			tb_findBook_condition.clear();

			this.displayBooks(bookService.getBooksByGenre(genre));
		} else if (cb_findBook.getValue() == "Condition") {
			String str_condition = tb_findBook_condition.getText();
			int condition;
			if (Validator.hasDigitsOnly(str_condition)) {
				condition = Integer.parseInt(str_condition);
				if (condition < 0) {
					Validator.showWrongInputAllert();
					tb_findBook_genre.clear();
					return;
				}
				this.displayBooks(bookService.getBookByCondition(condition));
			}

			tb_findBook_name.clear();
			tb_findBook_author.clear();
			tb_findBook_genre.clear();
		}

	}

	// add client block
	@FXML
	private TextField tb_addClient_name, tb_addClient_pass, tb_addClient_phone;

	/**Add Client to the database.
	 * @param event
	 */
	@FXML
	private void addClient(ActionEvent event) {
		String name = tb_addClient_name.getText();
		String pass = tb_addClient_pass.getText();
		String phone = tb_addClient_phone.getText();

		boolean status = true;
		status &= Validator.hasCharsOnly(name);
		status &= !Validator.isNullOrEmpty(pass);
		status &= Validator.checkPhone(phone);

		if (!status) {
			Validator.showWrongInputAllert();

			tb_addClient_name.clear();
			tb_addClient_pass.clear();
			tb_addClient_phone.clear();
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal2 = Calendar.getInstance();
		String date = dateFormat.format(cal2.getTime());

		Client cl = operator.createClient(name, pass, date, phone);

		userService.addClient(cl);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Success");
		alert.setHeaderText("CLient is inserted successfully ! ");
		alert.showAndWait();

		tb_addClient_name.clear();
		tb_addClient_pass.clear();
		tb_addClient_phone.clear();

	}

	// make rent block
	@FXML
	private TextField tb_makeRent_cname, tb_makeRent_bname, tb_makeRent_takeDate;

	/**Makes rent if possible
	 * If it is not possible shows alert box with the reason 
	 * @param event
	 */
	@FXML
	private void makeRent(ActionEvent event) {

		String book_name = tb_makeRent_bname.getText();
		String client_name = tb_makeRent_cname.getText();

		boolean status = true;
		status &= Validator.hasCharsOnly(book_name);
		status &= Validator.hasCharsOnly(client_name);

		if (!status) {
			Validator.showWrongInputAllert();

			tb_makeRent_bname.clear();
			tb_makeRent_cname.clear();
			return;
		}

		Book bk = bookService.getBookbyName(book_name).get(0);
		Client cl = userService.getClientByName(client_name);

		Rent r = new Rent(cl, bk);
		int statusRent = rentService.makeRent(r);

		if (statusRent == 0) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("SUCCESS");
			alert.setHeaderText("The rent is successfully made !!\n " + bk.getName() + " is given to " + cl.getName());
			alert.showAndWait();
		} else if (statusRent == 1)  {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("This book cannot be rented");
			alert.setHeaderText("This book is for the reading room ONLY!");
			alert.showAndWait();
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("FAIL");
			alert.setHeaderText("The rent cannot be made !!");
			alert.showAndWait();
			
		}
		tb_makeRent_bname.clear();
		tb_makeRent_cname.clear();
	}

	/**Closes rent if possible
	 * @param event
	 */
	@FXML
	private void closeRent(ActionEvent event) {
		String book_name = tb_makeRent_bname.getText();
		String client_name = tb_makeRent_cname.getText();

		boolean status = true;
		status &= Validator.hasCharsOnly(book_name);
		status &= Validator.hasCharsOnly(client_name);

		if (!status) {
			Validator.showWrongInputAllert();

			tb_makeRent_bname.clear();
			tb_makeRent_cname.clear();
			return;
		}

		Book bk = bookService.getBookbyName(book_name).get(0);
		Client cl = userService.getClientByName(client_name);

		Rent r = new Rent(cl, bk);
		int statusRent = rentService.closeRent(r);

		if (statusRent == 0) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("SUCCESS");
			alert.setHeaderText(
					"The rent is successfully cloesd !!\n " + cl.getName() + " returned book : " + bk.getName());
			alert.showAndWait();
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("FAIL");
			alert.setHeaderText("The rent cannot be cloesd !!");
			alert.showAndWait();
		}
		tb_makeRent_bname.clear();
		tb_makeRent_cname.clear();
	}

	@FXML
	private javafx.scene.control.TableColumn tc_class_name, tc_class_phone, tc_class_recDate, tc_class_loyalty;
	@FXML
	private TableView tw_class;

	/**Displays the result of Clients in the table view 
	 * @param res
	 */
	private void displayClients(List<Client> res) {

		tw_class.getItems().clear();
		if (res.isEmpty() || res == null) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("No Records");
			alert.setHeaderText("No records found !!");
			alert.showAndWait();
			return;
		}

		tc_class_name.setCellValueFactory(new PropertyValueFactory<Object, Object>("name"));
		tc_class_phone.setCellValueFactory(new PropertyValueFactory<Object, Object>("phoneNum"));
		tc_class_recDate.setCellValueFactory(new PropertyValueFactory<Object, Object>("recordDate"));
		tc_class_loyalty.setCellValueFactory(new PropertyValueFactory<Object, Object>("loyalty"));
		for (int i = 0; i < res.size(); i++) {

			tw_class.getItems().add(res.get(i));
		}
	}

	/**Shows the loyal clients in table view
	 * @param event
	 */
	@FXML
	private void classification_byLoyalty(ActionEvent event) {
		List<Client> res = userService.classifyClients("loyalty");
		displayClients(res);
	}

	/**Shows all clients sorted by registration date
	 * @param event
	 */
	@FXML
	private void classification_byRecDate(ActionEvent event) {
		List<Client> res = userService.classifyClients(" ");
		displayClients(res);
	}
	
	/**Shows the notification window when it is invoked from its button
	 * @param event
	 */
	@FXML
	private void getNotifications(ActionEvent event) {
		btn_getNotf.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
		FXMLLoader loader = new FXMLLoader();
		NotificationController nc = new NotificationController();
		loader.setController(nc);
		String address = "../../../views/NotificationPannel.fxml";
		Stage sys = new Stage();
		try {
		loader.setLocation(getClass().getResource(address));
			loader.load();
		} catch (IOException e) {       			
			e.printStackTrace();
		}
		Scene scene = new Scene((Parent) loader.getRoot());
		sys.setScene(scene);
		sys.show();
		sys.setResizable(false);
	}	
	
}
