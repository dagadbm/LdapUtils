package utils.ldap;

import java.util.List;

/**
 * Representa um atributo de utilizador num diretório LDAP.
 * @author E000161
 *
 */
public class UserAttribute {

	private String name;
	private AttributeType type;
	private AttributeOp op;

	private String singleValue;
	private List<String> multiValues;
	
	/**
	 * Gerar um UserAtribute sem valor. Utilizado para obter campos de utilizador a partir de pesquisa LDAP.
	 * @param name
	 * @param type
	 */
	public UserAttribute(String name, AttributeType type) {
		this.name = name;
		this.type = type;
		this.singleValue = null;
		this.multiValues = null;
		this.op = AttributeOp.SET_ATTRIBUTE;
	}
	
	/**
	 * Gerar um UserAtribute para eliminação. Construtor utilizador quando se quer eliminar um atributo no diretorio.
	 * @param name
	 * @param type
	 * @param op
	 */
	public UserAttribute(String name, AttributeType type, AttributeOp op) {
		this.name = name;
		this.type = type;
		this.singleValue = null;
		this.multiValues = null;
		this.op = op;
	}
	
	/**
	 * Gerar um UserAttribute para modificação. Construtor utilizado para fazer modificações de um atributo single valued. Opcionalmente pode-se designar qual o tipo de modificação LDAP a fazer.\r\n
	 * Por defeito esta operação é a de substituir o campo no diretório LDAP com o valor do atributo.
	 * @param name
	 * @param singleValue
	 * @param op 
	 */
	public UserAttribute(String name, String singleValue, AttributeOp ... op) {
		this.name = name;
		this.type = AttributeType.SINGLE_VALUED;
		this.singleValue = singleValue;
		this.multiValues = null;
		if(op.length == 1) {
			this.op = op[0];
		} else {
			this.op = AttributeOp.SET_ATTRIBUTE;
		}
	}
	
	/**
	 * Gerar um UserAttribute para modificação. Construtor utilizado para fazer modificações de um atributo multi valued. Opcionalmente pode-se designar qual o tipo de modificação LDAP a fazer.\r\n
	 * Por defeito esta operação é a de substituir o campo no diretório LDAP com o valor do atributo.
	 * @param name
	 * @param multiValues
	 * @param op
	 */
	public UserAttribute(String name, List<String> multiValues, AttributeOp ... op) {
		this.name = name;
		this.type = AttributeType.MULTI_VALUED;
		this.singleValue = null;
		this.multiValues = multiValues;
		if(op.length == 1) {
			this.op = op[0];
		} else {
			this.op = AttributeOp.SET_ATTRIBUTE;
		}
	}
		
	/**
	 * Obtem o nome do atributo no diretório LDAP.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Define o nome do atributo no diretório LDAP.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Obtem o tipo de atributo no diretório LDAP.
	 */
	public AttributeType getType() {
		return type;
	}

	/**
	 * Define o tipo de atributo no diretório LDAP.
	 */
	public void setType(AttributeType type) {
		this.type = type;
	}
	/**
	 * Obtem o tipo de operação a ser efetuada no diretório LDAP.
	 * @return
	 */
	public AttributeOp getOp() {
		return op;
	}

	/**
	 * Definir o tipo de operaço a ser efetuada no diretório LDAP.
	 * @param modType
	 */
	public void setOp(AttributeOp attributeOp) {
		this.op = attributeOp;
	}
	
	/**
	 * Obtém o valor single-valued do atributo. Caso o atributo seja multi-valued é lançada uma UnsupportedOperationException.
	 * @return
	 */
	public String getSingleValue() {
		if(type == AttributeType.SINGLE_VALUED) {
			return singleValue;
		} else {
			throw new UnsupportedOperationException("Not a single-valued UserAttribute.");
		}
	}

	/**
	 * Set ao valor single-valued do atributo.
	 * @param singleValue
	 */
	public void setSingleValue(String singleValue) {
		if(type == AttributeType.SINGLE_VALUED) {
			this.singleValue = singleValue;
		} else {
			throw new UnsupportedOperationException("Not a single-valued UserAttribute.");
		}
	}
	
	/**
	 * Obtém os valores multi-valued do atributo. Caso o atributo seja single-valued é lançada uma UnsupportedOperationException.
	 * @return
	 */
	public List<String> getMultiValues() {
		if(type == AttributeType.MULTI_VALUED) {
			return multiValues;
		} else {
			throw new UnsupportedOperationException("Not a multi-valued UserAttribute.");
		}
	}

	/**
	 * Set ao valor multi-valued to atributo.
	 * @param multiValues
	 */
	public void setMultiValues(List<String> multiValues) {
		if(type == AttributeType.MULTI_VALUED) {
			this.multiValues = multiValues;
		} else {
			throw new UnsupportedOperationException("Not a multi-valued UserAttribute.");
		}
	}
	
	/**
	 * Imprime uma descrição do UserAttribute
	 */
	@Override
	public String toString() {
		String result = "UserAttribute [name=" + name + ", modOp=" + op + ", type=" + type; 
		if(type == AttributeType.SINGLE_VALUED) {
			result += ", singleValue=" + singleValue;
		} else if(type == AttributeType.MULTI_VALUED) {
			result += ", multiValues=" + multiValues;
		}
		result += "]\r\n";
		return result;
	}
	
	/**
	 * Devolve true se e só se o userAttr tiver o mesmo conteudo do atributo que a instancia em questão.
	 * Funciona tanto para multi-values (devolve true mesmo que a ordem dos valores seja diferente) como para single-values
	 */
	public boolean hasSameValue(UserAttribute userAttr) {
		if(userAttr.type.equals(AttributeType.SINGLE_VALUED)) {
			if(this.type.equals(AttributeType.SINGLE_VALUED)) { 
				String argValue = userAttr.getSingleValue();
				String objValue = this.getSingleValue();
				if(argValue != null && objValue != null) {
					return userAttr.getSingleValue().equals(this.getSingleValue());
				} else {
					if( (argValue == null && objValue != null) || (argValue != null && objValue == null) ) { 
						return false;
					} else {
						return true;
					}
				}
			} else {
				throw new UnsupportedOperationException("Trying to compare userAttr single-valued attribute with class multi-valued attribute.");
			}
		} else if(userAttr.type.equals(AttributeType.MULTI_VALUED)) {
			if(this.type.equals(AttributeType.MULTI_VALUED)) {
				boolean result = true;
				List<String> argMultiValue = userAttr.getMultiValues();
				List<String> objMultiValue = this.getMultiValues();
				if(argMultiValue != null && objMultiValue != null) {
					if(objMultiValue.size() == argMultiValue.size()) { // Apenas compara elemento a elemento se tiverem o mesmo tamanho. Caso contrário são diferentes
						for(int i = 0; i < argMultiValue.size(); i++) {
							if(!objMultiValue.contains(argMultiValue.get(i))) {
								result = false;
								break;
							}
						}
					} else {
						result = false;
					}
					return result;
				} else {
					if( (argMultiValue == null && objMultiValue != null) || (argMultiValue != null && objMultiValue == null) ) { 
						return false;
					} else {
						return true;
					}				
				}
			} else {
				throw new UnsupportedOperationException("Trying to compare userAttr multi-valued attribute with class single-valued attribute.");
			}
		}
		throw new UnsupportedOperationException("userAttr is neither single-valued nor multi-valued.");
	}
}