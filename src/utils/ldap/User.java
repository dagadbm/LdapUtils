package utils.ldap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe que representa um utilizador num diretório LDAP.
 * 
 * @author E000161
 *
 */
public class User {

	private String distinguishedName;
	private String friendlyName;
	private Map<String, UserAttribute> userAttributes;

	/**
	 * Utilizador sem atributos.
	 * 
	 * @param distinguishedName
	 * @param friendlyName
	 */
	public User(String distinguishedName, String friendlyName) {
		super();
		this.distinguishedName = distinguishedName;
		this.friendlyName = friendlyName;
		this.userAttributes = new LinkedHashMap<String, UserAttribute>();
	}

	/**
	 * Utilizador com atributos.
	 * 
	 * @param distinguishedName
	 * @param friendlyName
	 * @param userAttributes
	 */
	public User(String distinguishedName, String friendlyName, List<UserAttribute> userAttributes) {
		super();
		this.distinguishedName = distinguishedName;
		this.friendlyName = friendlyName;
		this.userAttributes = new LinkedHashMap<String, UserAttribute>();
		for (UserAttribute attr : userAttributes) {
			this.userAttributes.put(attr.getName(), attr);
		}
	}

	/**
	 * Obtem o DistinguishedName (DN) do utilizador no diretório LDAP
	 * 
	 * @return
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}

	/**
	 * Altera o DistinguishedName (DN) do utilizador no diretório LDAP
	 * 
	 * @param distinguishedName
	 */
	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	/**
	 * Obtem o friendlyName (identificador) do utilizador no diretório LDAP
	 * 
	 * @return
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * Altera o friendlyName (identificador) do utilizador no diretório LDAP
	 * 
	 * @param friendlyName
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * Obtem a lista dos atributos do utilizador.
	 * Utilizar apenas para visualização dos atributos.
	 * Caso seja necessário fazer algum set em especifico utilizar os métodos próprios para o efeito.
	 * 
	 * @return
	 */
	public List<UserAttribute> getUserAttributes() {
		return new ArrayList<UserAttribute>(userAttributes.values());
	}

	/**
	 * Faz set a todos os user attributes do utilizador limpando todos os atributos anteriores utilizando internamente o método clearUserAttributes().
	 * 
	 * @param userAttributes
	 */
	public void setUserAttributes(List<UserAttribute> userAttributes) {
		clearUserAttributes();
		for (UserAttribute attr : userAttributes) {
			this.userAttributes.put(attr.getName(), attr);
		}
	}

	/**
	 * Adiciona um userAttribute ao utilizador. Se o user attribute já existir actualiza o seu valor.
	 * 
	 * @param userAttribute
	 */
	public void addUserAttribute(UserAttribute userAttribute) {
		this.userAttributes.put(userAttribute.getName(), userAttribute);
	}

	/**
	 * Limpa todos os UserAttributes do utilizador.
	 */
	public void clearUserAttributes() {
		this.userAttributes.clear();
	}

	/**
	 * Devolve um UserAttribute a partir do seu nome. Devolve null se nenhum atributo for encontrado. Case-sensitive.
	 * 
	 * @param name
	 * @return
	 */
	public UserAttribute getUserAttributeByName(String name) {
		return this.userAttributes.get(name);
	}

	/**
	 * Faz set ao single value attribute com um valor diferente. Case-sensitive.</p>
	 * Se este atributo não existir é adicionado ao utilizador.
	 * 
	 * @param name
	 * @param value
	 */
	public void setSingleValuedUserAttributeByName(String name, String value) {
		UserAttribute attr = this.userAttributes.get(name);
		if (attr == null) {
			this.userAttributes.put(name, new UserAttribute(name, value));
		} else {
			attr.setSingleValue(value);
		}
	}

	/**
	 * Faz set ao multi value attribute com um valor diferente. Case-sensitive</p>
	 * Se este atributo não existir é adicionado ao utilizador.
	 * 
	 * @param name
	 * @param values
	 */
	public void setMultiValuedUserAttributeByName(String name, List<String> values) {
		UserAttribute attr = this.userAttributes.get(name);
		if (attr == null) {
			this.userAttributes.put(name, new UserAttribute(name, values));
		} else {
			attr.setMultiValues(values);
		}
	}

	/**
	 * Altera o nome do multi-value para um valor diferente mantendo os valores do atributo. Funciona tanto para single-valued como para multi-valued.
	 * 
	 * @param previousName
	 * @param newName
	 */
	public void replaceUserAttributeName(String previousName, String newName) {
		UserAttribute newAttr = this.userAttributes.remove(previousName);
		newAttr.setName(newName);
		this.userAttributes.put(newName, newAttr);
	}

	/**
	 * Imprime um User com o seu FriendlyName seguido dos seus atributos invocando o toString() de cada um dos UserAttributes do utilizador.
	 */
	@Override
	public String toString() {
		return "[User " + friendlyName + "\r\n " + userAttributes.values();
	}
}
