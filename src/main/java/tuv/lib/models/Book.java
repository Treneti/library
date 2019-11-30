package tuv.lib.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javassist.expr.NewArray;

public class Book {
	int id;
	String name;
	String invNumber;
	String genre;
	int condition;
	List<String> authors;
	// String authors;

	public Book(String name, int cond) {
		this(name, null, "", "", cond);
	}

	public Book(String name, List<String> authors, String genre, String invNum) {
		this(name, authors, genre, invNum, 0);
	}

	public Book(String name, List<String> authors, String genre, String invNum, int condition) {
		this.authors = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return String.join(", ", this);
			}
		};
		this.name = name;
		this.invNumber = invNum;
		this.genre = genre;
		this.condition = condition;
		if (authors != null) {
			this.authors.addAll(authors);
		}
	}

	public Book(String name) {
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInvNumber() {
		return this.invNumber;
	}

	public void setInvNumber(String invNumber) {
		this.invNumber = invNumber;
	}

	public String getGenre() {
		return this.genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getCondition() {
		return this.condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public List<String> getAuthors() {
		return this.authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
	
	
}
