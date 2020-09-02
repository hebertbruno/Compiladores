

public class Parser {
	public static final int _EOF = 0;
	public static final int _numero = 1;
	public static final int _nomeProprio = 2;
	public static final int _palavra = 3;
	public static final int maxT = 15;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void Familia() {
		Secao();
		while (StartOf(1)) {
			Secao();
		}
	}

	void Secao() {
		switch (la.kind) {
		case 4: {
			Sobrenome();
			break;
		}
		case 6: {
			Pais();
			break;
		}
		case 8: {
			Filhos();
			break;
		}
		case 9: {
			Avos();
			break;
		}
		case 10: {
			Netos();
			break;
		}
		case 11: {
			Outros();
			break;
		}
		default: SynErr(16); break;
		}
	}

	void Sobrenome() {
		Expect(4);
		Expect(5);
		Nome();
	}

	void Pais() {
		Expect(6);
		Expect(5);
		Nome();
		while (la.kind == 7) {
			Get();
			Nome();
		}
	}

	void Filhos() {
		Expect(8);
		Expect(5);
		Nome();
		while (la.kind == 7) {
			Get();
			Nome();
		}
	}

	void Avos() {
		Expect(9);
		Expect(5);
		Nome();
		while (la.kind == 7) {
			Get();
			Nome();
		}
	}

	void Netos() {
		Expect(10);
		Expect(5);
		Nome();
		while (la.kind == 7) {
			Get();
			Nome();
		}
	}

	void Outros() {
		Expect(11);
		Expect(5);
		Item();
		while (la.kind == 7) {
			Get();
			Item();
		}
	}

	void Nome() {
		Expect(2);
		while (la.kind == 2 || la.kind == 3) {
			if (la.kind == 2) {
				Get();
			} else {
				Get();
			}
		}
		Comentario();
	}

	void Item() {
		Expect(1);
		Frase();
	}

	void Comentario() {
		if (la.kind == 12) {
			Get();
		}
		if (la.kind == 13) {
			Get();
			Frase();
			Expect(14);
		}
	}

	void Frase() {
		Expect(3);
		while (la.kind == 2 || la.kind == 3) {
			if (la.kind == 3) {
				Get();
			} else {
				Get();
			}
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Familia();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,x,x,x, T,x,T,x, T,T,T,T, x,x,x,x, x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "numero expected"; break;
			case 2: s = "nomeProprio expected"; break;
			case 3: s = "palavra expected"; break;
			case 4: s = "\"Sobrenome\" expected"; break;
			case 5: s = "\":\" expected"; break;
			case 6: s = "\"Pais\" expected"; break;
			case 7: s = "\",\" expected"; break;
			case 8: s = "\"Filhos\" expected"; break;
			case 9: s = "\"Avos\" expected"; break;
			case 10: s = "\"Netos\" expected"; break;
			case 11: s = "\"Outros\" expected"; break;
			case 12: s = "\"(falecido)\" expected"; break;
			case 13: s = "\"[\" expected"; break;
			case 14: s = "\"]\" expected"; break;
			case 15: s = "??? expected"; break;
			case 16: s = "invalid Secao"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
