package tuv.lib.models.interfaces;

import java.util.List;

import tuv.lib.models.Client;
import tuv.lib.models.User;

public interface UserService {
	public void addUser(User u);
	public List<Client> findClients(String name);
	public void addClient(Client cl);
	public void removeUser(int id);
	public int getUserPos(String name, String password);
}
