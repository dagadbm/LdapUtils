package utils.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

/**
 * Classe que abstrai toda a lógica de diretórios LDAP.
 * 
 * @author E000161
 *
 */
public class LdapProxy {

	private String url;
	private String distinguishedName;
	private String password;
	private int searchLimit;
	private boolean isPagedSearch;
	private LdapContext ctx;

	/**
	 * Criar um LDAPProxy. Recebe uma connection string ldap: url , dn e password assim como um limite de pesquisa para pesquisas paginadas. Utilizar 0 caso não seja necessário pesquisas paginadas.</p>
	 * Para inicializar invocar primeiro o método openConnection().</p>
	 * Para finalizar invocar por último o método closeConnection().
	 * 
	 * @param url
	 * @param distinguishedName
	 * @param password
	 * @param searchLimit
	 *            usar 0 caso não seja pretendido pesquisas paginadas.
	 */
	public LdapProxy(String url, String distinguishedName, String password, int searchLimit) {
		this.url = url;
		this.distinguishedName = distinguishedName;
		this.password = password;
		ctx = null;

		if (searchLimit == 0) {
			isPagedSearch = false;
		} else {
			isPagedSearch = true;
			this.searchLimit = searchLimit;
		}
	}

	/**
	 * Devolve o primeiro utilizador encontrado no diretório. Internamente este método invoca getUsers e devolve o primeiro elemento da lista e null caso não tenha sido encontrado nenhum utilizador.</p>
	 * O campo friendlyNameAttribute, quando difernete de null significa que o valor desse campo será utilizado para definir o friendly name do utilizador. Caso o valor não exista o friendlyName é preenchido com null.</p>
	 * Para mais detalhe ver a função getUsers().
	 * 
	 * @param baseDN
	 * @param filter
	 * @param userAttributes
	 * @return
	 * @throws IOException
	 * @throws NamingException
	 */
	public User getUser(String baseDN, String filter, List<UserAttribute> userAttributes, UserAttribute friendlyNameAttribute) throws NamingException, IOException {
		List<User> result = getUsers(baseDN, filter, userAttributes, friendlyNameAttribute);
		if (result == null) {
			return null;
		} else {
			return result.get(0); // Devolve o primeiro elemento.
		}
	}

	/**
	 * Devolve uma lista de utilizadores com base no filtro. Estes utilizadores terão os UserAttributes deles preenchidos conforme o argumento userAttributes.</p>
	 * O friendlyName do utilizador é o primeiro nome do container LDAP ( convertido para MINUSCULAS). Por exemplo o utilizador com o DN: OU=Nome,OU=abc,OU=cde,OU=fgh terá como friendlyName o valor "nome".</p>
	 * Caso o utilizador não tenha o atributo pedido preenchido no diretorio LDAP este atributo é preenchido a null.</p>
	 * Caso não tenham sido encontrados utilizadores é devolvido null.</p>
	 * Se o userAttributes for null é devolvida a lista de utilizadores sem atributos preenchidos.</p>
	 * O campo friendlyNameAttribute, quando difernete de null significa que o valor desse campo será utilizado para definir o friendly name do utilizador. Caso o valor não exista o friendlyName é preenchido com null.
	 * 
	 * @param baseDN
	 * @param filter
	 * @param userAttributes
	 * @return
	 * @throws IOException
	 * @throws NamingException
	 */
	public List<User> getUsers(String baseDN, String filter, List<UserAttribute> userAttributes, UserAttribute friendlyNameAttribute) throws NamingException, IOException {

		List<User> result = new ArrayList<User>();
		List<String> retAttrs = new ArrayList<String>();
		boolean hasFoundUsers = false;

		if (userAttributes != null) {
			for (UserAttribute userAttribute : userAttributes) {
				retAttrs.add(userAttribute.getName());
			}
			if (friendlyNameAttribute != null) {
				retAttrs.add(friendlyNameAttribute.getName());
			}
		} else {
			if (friendlyNameAttribute != null) {
				retAttrs.add(friendlyNameAttribute.getName());
			} else {
				retAttrs = null;
			}
		}

		List<SearchResult> results = ldapSearch(baseDN, filter, (retAttrs == null ? new String[] {} : retAttrs.toArray(new String[retAttrs.size()])));

		try {
			for (SearchResult searchResult : results) {
				Attributes attributes = searchResult.getAttributes();
				String dn = searchResult.getNameInNamespace();
				String friendlyName = "";
				if (friendlyNameAttribute == null) {
					friendlyName = dn.substring(dn.indexOf("=") + 1, dn.indexOf(","));
				} else {
					Attribute singleValuedFieldAttr = attributes.get(friendlyNameAttribute.getName());
					friendlyName = (String) (singleValuedFieldAttr == null ? null : singleValuedFieldAttr.get());
				}
				List<UserAttribute> userAttributesResult = new ArrayList<UserAttribute>();
				hasFoundUsers = true;

				// Prepare all returned attributes
				if (userAttributes != null) {
					for (UserAttribute userAttribute : userAttributes) {
						String fieldName = userAttribute.getName();
						AttributeType fieldType = userAttribute.getType();

						if (fieldType == AttributeType.SINGLE_VALUED) {
							Attribute singleValuedFieldAttr = attributes.get(fieldName);
							String singleValuedAttr = (String) (singleValuedFieldAttr == null ? null : singleValuedFieldAttr.get());
							userAttributesResult.add(new UserAttribute(fieldName, singleValuedAttr));
						} else if (fieldType == AttributeType.MULTI_VALUED) {
							Attribute multiValuedFieldAttr = attributes.get(fieldName);
							List<String> multiValuedAttr = new ArrayList<String>();
							if (multiValuedFieldAttr != null) {
								NamingEnumeration<?> multiValuedFieldAttrEnum = multiValuedFieldAttr.getAll();
								while (multiValuedFieldAttrEnum.hasMore()) {
									multiValuedAttr.add(multiValuedFieldAttrEnum.next().toString());
								}
							} else {
								multiValuedAttr = null;
							}
							userAttributesResult.add(new UserAttribute(fieldName, multiValuedAttr));
						}
					}
					// Add attributes to user
					result.add(new User(dn, friendlyName.toLowerCase(), userAttributesResult));
				} else {
					result.add(new User(dn, friendlyName.toLowerCase()));
				}
			}
		} catch (NamingException e) {
			throw e;
		}

		if (hasFoundUsers) {
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Modifica apenas um utilizador. Internamente invoca modifyUsers com uma lista de um utilizador.
	 * 
	 * @param ctx
	 * @param user
	 * @throws NamingException
	 */
	public void modifyUser(User user) throws NamingException {
		List<User> userList = new ArrayList<User>();
		userList.add(user);
		modifyUsers(userList);
	}

	/**
	 * Modifica os utilizadores pedidos. Cada atributo será um item de modificação LDAP com o tipo de modificação obtido através do AttributeOp do UserAttribute.</p>
	 * Se o o AttributeOp = CLEAR_ATTRIBUTE então o campo será limpo do utilizador sendo ignorado o valor posto no atributo.</p>
	 * Se o AttributeOp = ADD_ATTRIBUTE então é adicionado o valor ao campo quer este campo seja considerado SINGLE ou MULTI pelo diretório LDAP. Caso o valor a adicionar seja duplicado então é lançada uma excepção de LDAP com o nome AttributeInUseException.</p>
	 * Se o AttributeOp = SET_ATTRIBUTE então o campo do utilizador será substituido pelo valor do UserAttribute no diretório LDAP.
	 * 
	 * @param ctx
	 * @param users
	 * @throws NamingException
	 */
	public void modifyUsers(List<User> users) throws NamingException {
		// Para cada utilizador
		for (User user : users) {
			String dn = user.getDistinguishedName();
			List<UserAttribute> userAttributes = user.getUserAttributes();
			List<ModificationItem> modItems = new ArrayList<ModificationItem>();

			// Tratar de cada campo individualmente a ser modificado
			for (UserAttribute userAttribute : userAttributes) {
				BasicAttribute basicAttributeAttr = null;
				AttributeOp modOp = userAttribute.getOp();
				if (modOp != AttributeOp.CLEAR_ATTRIBUTE) {
					if (userAttribute.getType() == AttributeType.SINGLE_VALUED) {
						String userAttr = userAttribute.getSingleValue();
						if (userAttr == null) {
							basicAttributeAttr = new BasicAttribute(userAttribute.getName());
						} else {
							basicAttributeAttr = new BasicAttribute(userAttribute.getName(), userAttr);
						}
					} else if (userAttribute.getType() == AttributeType.MULTI_VALUED) {
						List<String> userAttrs = userAttribute.getMultiValues();
						basicAttributeAttr = new BasicAttribute(userAttribute.getName());
						if (userAttrs != null) {
							for (String attr : userAttrs) {
								if (attr != null) {
									basicAttributeAttr.add(attr);
								}
							}
						}
					}
				} else {
					basicAttributeAttr = new BasicAttribute(userAttribute.getName());
				}

				switch (modOp) {
				case ADD_ATTRIBUTE:
					modItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, basicAttributeAttr));
					break;
				case SET_ATTRIBUTE:
					modItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, basicAttributeAttr));
					break;
				case CLEAR_ATTRIBUTE:
					modItems.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, basicAttributeAttr));
					break;
				default:
					break;
				}
			}
			ldapModify(dn, modItems.toArray(new ModificationItem[modItems.size()]));
		}
	}

	/**
	 * Método interno para pesquisas LDAP.
	 * 
	 * @param baseDN
	 * @param filter
	 * @param retAttrs
	 * @return
	 * @throws NamingException
	 * @throws IOException
	 */
	private List<SearchResult> ldapSearch(String baseDN, String filter, String[] retAttrs) throws NamingException, IOException {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setReturningAttributes(retAttrs);

		// Paged ldap search
		if (isPagedSearch) {

			List<SearchResult> resultList = new ArrayList<SearchResult>();
			byte[] cookie = null;

			try {

				ctx.setRequestControls(new Control[] { new PagedResultsControl(searchLimit, Control.NONCRITICAL) });
				do {
					NamingEnumeration<SearchResult> pagedResults = ctx.search(baseDN, filter, searchControls);
					resultList.addAll(Collections.list(pagedResults));

					Control[] pagedControls = ctx.getResponseControls();
					if (pagedControls != null) {
						for (int i = 0; i < pagedControls.length; i++) {
							if (pagedControls[i] instanceof PagedResultsResponseControl) {
								cookie = ((PagedResultsResponseControl) pagedControls[i]).getCookie();
							}
						}
					}
					ctx.setRequestControls(new Control[] { new PagedResultsControl(searchLimit, cookie, Control.CRITICAL) });
				} while (cookie != null);

				return resultList;
			} catch (NamingException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		} else {
			try {
				NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, searchControls);
				List<SearchResult> resultList = Collections.list(results);
				return resultList;
			} catch (NamingException e) {
				throw e;
			}
		}
	}

	/**
	 * Método interno para modificações LDAP.
	 * 
	 * @param dn
	 * @param modItems
	 * @throws NamingException
	 */
	private void ldapModify(String dn, ModificationItem[] modItems) throws NamingException {
		try {
			ctx.modifyAttributes(dn, modItems);
		} catch (NamingException e) {
			throw e;
		}
	}

	/**
	 * Inicia connecção LDAP. Utilizar no inicio.
	 * 
	 * @throws NamingException
	 */
	public void openLdapConnection() throws NamingException {
		Hashtable<String, String> setup = new Hashtable<String, String>();
		setup.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		setup.put(Context.PROVIDER_URL, "ldap://" + url);
		setup.put(Context.SECURITY_AUTHENTICATION, "simple");
		setup.put(Context.SECURITY_PRINCIPAL, distinguishedName);
		setup.put(Context.SECURITY_CREDENTIALS, password);
		setup.put("com.sun.jndi.ldap.connect.pool", "true");
		ctx = new InitialLdapContext(setup, null);
	}

	/**
	 * Fecha connecção LDAP. Utilizar no final.
	 * 
	 * @throws NamingException
	 */
	public void closeLdapConnection() throws NamingException {
		ctx.close();
	}
}