/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jormary.projeto_bot_java;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Wallace Goncalves
 */
public class ValidadorNumerico extends PlainDocument {

    private static final long serialVersionUID = 1L;
    private int limite;

    public ValidadorNumerico(int limite) {
        super();
        this.limite = limite;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }

        // Verifica se o comprimento após a inserção não excede o limite
        if ((getLength() + str.length()) <= limite) {
            StringBuilder texto = new StringBuilder(getText(0, getLength()));

            // Verifica cada caractere da string inserida
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                // Verifica se o caractere é um número, um ponto decimal ou um sinal de menos (para números negativos)
                if (Character.isDigit(c) || c == '.' || c == '-') {
                    // Verifica se já existe um ponto decimal ou sinal de menos
                    if ((c == '.' || c == '-') && (texto.toString().contains(".") || texto.toString().contains("-"))) {
                        return;
                    }
                    texto.append(c);
                }
            }

            // Substitui o texto pelo texto validado
            super.remove(0, getLength());
            super.insertString(0, texto.toString(), attr);
        }
    }
}
