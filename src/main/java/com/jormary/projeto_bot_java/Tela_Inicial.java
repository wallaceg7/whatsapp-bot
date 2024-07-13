/*
 * The MIT License
 *
 * Copyright 2023 Wallace Goncalves.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jormary.projeto_bot_java;
import com.formdev.flatlaf.FlatLightLaf; // Importe a classe FlatLaf
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.CellType;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
/**
 *
 * @author Wallace Goncalves
 */
public class Tela_Inicial extends javax.swing.JFrame {

    /**
     * Creates new form Tela_Inicial
     */
    private SwingWorker<Void, Integer> worker;
    ArrayList<String> LISTA_ENVIADOS = null;
    
    // Referência: https://www.youtube.com/watch?v=2HNdKafmpSw
    
    // Elementos XPATH dos botões e inputs HTML
    String XPATH_OPCOES = "//*[@id=\"main\"]/footer/div[1]/div/span[2]/div/div[1]/div/div/div/div/span";
    String XPATH_INPUT = "//*[@id=\"main\"]/footer/div[1]/div/span[2]/div/div[1]/div/div/span/div/ul/div/div[2]/li/div/input";
    String XPATH_CLICK_ENVIARFOTO = "//*[@id=\"app\"]/div/div[2]/div[2]/div[2]/span/div/span/div/div/div[2]/div/div[2]/div[2]/div/div/span";
    
    // Variavel de controle do intervalo de envios
    int num_anterior = 0;

    // Total de numeros e andamento atual
    int tot_numeros = 0, andamento = 0;
    
    boolean ativado = false;

    public Tela_Inicial() {
        initComponents();
        setResizable(false);
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/icones/icone12.png"));
        setIconImage(icon.getImage());
        
        lbIntervalo.setVisible(false);
        totalContatos.setText("");
        
        txtMensagem.setDocument(new ValidadorLimite(800));
        
        tempoAtualizacaoPag.setDocument(new ValidadorNumerico(60000));
        tempoVerificacaoNumero.setDocument(new ValidadorNumerico(60000));
        tempoCliqueOpcoes.setDocument(new ValidadorNumerico(60000));
        tempoSelecaoFoto.setDocument(new ValidadorNumerico(60000));
        tempoMinIntervalo.setDocument(new ValidadorNumerico(20));
        tempoMaxIntervalo.setDocument(new ValidadorNumerico(60));
        tempoPosEnvio.setDocument(new ValidadorNumerico(60000));
        
        tempoAtualizacaoPag.setText("4000");
        tempoVerificacaoNumero.setText("2000");
        tempoCliqueOpcoes.setText("1500");
        tempoSelecaoFoto.setText("1500");
        tempoMinIntervalo.setText("10");
        tempoMaxIntervalo.setText("30");
        tempoPosEnvio.setText("3000");

    }
    
    // Função para selecionar o diretório PNG
    private void SelecionarPNG(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo PNG");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos PNG", "png");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            dirPng.setText(selectedFile.getAbsolutePath());
            // Seta a imagem para a label
            ImageIcon iconLogo = new ImageIcon(selectedFile.getAbsolutePath());
            labelImagem.setIcon(iconLogo);
        }
    }
    
    // função para selecionar o diretório do Excel
    private void SelecionarExcel(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo XLSX");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos XLSX", "xlsx");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            dirExcel.setText(selectedFile.getAbsolutePath());
            try{
                loadExcelData(selectedFile);
            } catch (IOException e){
                JOptionPane.showMessageDialog(null, "Falha ao carregar Excel");
            }
        }
        totalContatos.setText((tbContatos.getRowCount() > 0)?tbContatos.getRowCount()+" contatos":"");
    }
    
    private void loadExcelData(File file_ex) throws IOException {
        
        FileInputStream fis = new FileInputStream(file_ex);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        int cols = sheet.getRow(0).getPhysicalNumberOfCells();
        String[][] data = new String[rows][cols];

        for (int r = 0; r < rows; r++) {
            XSSFRow row = sheet.getRow(r);
            for (int c = 0; c < cols; c++) {
                XSSFCell cell = row.getCell(c);
                if (cell != null) {
                    // Verifica se o tipo de célula é numérico e não vazio
                    if (cell.getCellType() == CellType.NUMERIC) {
                        // Converte o número de telefone para string
                        data[r][c] = String.format("%.0f", cell.getNumericCellValue());
                    } else {
                        data[r][c] = cell.toString();
                    }
                } else {
                    data[r][c] = "";
                }
            }
        }
        // Definindo o modelo da tabela
        String[] columnNames = new String[cols];
        for (int c = 0; c < cols; c++) {
            columnNames[c] = "Contatos";
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        tbContatos.setModel(model);

        fis.close();
        workbook.close();
    }
    
    private void SelecionaDriver(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo EXE");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos EXE", "exe");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            dirDriver.setText(selectedFile.getAbsolutePath());
        }
    }
    
    // função para gerar um novo intervalo de clique a partir de limite minimo e maximo
    private static int Intervalo(int min, int max){
        Random random = new Random();
        return (random.nextInt(max - min + 1) + min)* 1000;
    }
    
    private void Inicializacao(){
        
        int total_contatos = tbContatos.getRowCount();
        LISTA_ENVIADOS = new ArrayList<>();
        btnPng.setEnabled(false);
        btnExcel.setEnabled(false);
        txtMensagem.setEditable(false);
        btnLimpar.setEnabled(false);
        btnPasta.setEnabled(false);
        btnTeste.setEnabled(false);
        btnEnviar.setEnabled(false);
        
        if ((dirDriver.getText().isEmpty()) || (dirPng.getText().isEmpty()) || (dirExcel.getText().isEmpty()) || (txtMensagem.getText().isEmpty() || (total_contatos == 0))){
            JOptionPane.showMessageDialog(null, "Preencha todos os campos!");
        } else {
            
            // intervalo para range random [min a max]
            int min = Integer.parseInt(tempoMinIntervalo.getText());
            int max = Integer.parseInt(tempoMaxIntervalo.getText());

            // Tempos de espera
            int tempo_posEnvioFoto = Integer.parseInt(tempoPosEnvio.getText());
            
            int tempo_posAtPagina = Integer.parseInt(tempoAtualizacaoPag.getText());
            int tempo_posVerificaNum = Integer.parseInt(tempoVerificacaoNumero.getText());
            int tempo_cliqueOpcoes = Integer.parseInt(tempoCliqueOpcoes.getText());
            int tempo_selecaoFoto = Integer.parseInt(tempoSelecaoFoto.getText());
            
            System.setProperty("webdriver.chrome.driver",dirDriver.getText());
            
            String formato = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat dataMascara = new SimpleDateFormat(formato);
            String data = dataMascara.format(new Date());
            
            String caminhoImagem = dirPng.getText();
            String caminhoExcel = dirExcel.getText();
            String mensagemCodificada = URLEncoder.encode(txtMensagem.getText(), StandardCharsets.UTF_8);
            
            try{
            
                WebDriver navegador = new ChromeDriver();
                navegador.get("https://web.whatsapp.com");
                
                andamento = 0;
                tot_numeros = total_contatos;
                //AtualizacaoProgresso();
                lbIntervalo.setVisible(true);
                progresso.setMinimum(0);
                progresso.setMaximum(total_contatos);
                lbAndamento.setText(0+" de "+total_contatos+" enviados...");

                // Inicia a execução em uma SwingWorker para tarefas em segundo plano
                worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Aguarda a entrada com QRCode
                        while(navegador.findElements(By.id("side")).isEmpty()){
                            Thread.sleep(200);
                            System.out.println("Carregando");
                        }

                        for (int row = 0; row < tbContatos.getRowCount(); row++) {

                            andamento = row + 1;
                            publish(andamento);

                            Thread.sleep(4000);
                            String contato = tbContatos.getValueAt(row, 0).toString();
                            boolean travou = false;

                            String link = "https://web.whatsapp.com/send?phone="+contato+"&text="+mensagemCodificada;
                            System.out.println("\nLink codificado : "+link);
                            navegador.get(link);

                            while(navegador.findElements(By.id("side")).isEmpty()){
                                Thread.sleep(100); // 1000 milissegundos = 1 segundo
                                System.out.println("Carregando");
                            }
                            Thread.sleep(tempo_posAtPagina); // 1000 milissegundos = 1 segundo
                            System.out.println("------ Verificacao da tela");

                            while((!navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).isEmpty()) && (!travou)){
                                Object elementoObj = navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).get(0);
                                String elemento = elementoObj != null? elementoObj.toString(): "O número de telefone compartilhado por url é inválido.";
                                System.out.println("Aguardando, Texto do elemento: "+elemento);
                                travou = elemento.equals("O número de telefone compartilhado por url é inválido.");
                                Thread.sleep(500); // 1000 milissegundos = 1 segundo
                            }
                            Thread.sleep(tempo_posVerificaNum);

                            System.out.println("Pós atualizacao");

                            if(navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).isEmpty()){
                                System.out.println("Inicia seleção");
                                //# BOTÃO OPÇÕES DE ENVIO
                                navegador.findElement(By.xpath(XPATH_OPCOES)).click();
                                System.out.println("Click opçoes");
                                Thread.sleep(tempo_cliqueOpcoes);

                                // # BOTÃO SELECIONAR FOTO
                                navegador.findElement(By.xpath(XPATH_INPUT)).sendKeys(dirPng.getText());
                                System.out.println("Selecionou foto");
                                Thread.sleep(tempo_selecaoFoto);

                                //REALIZA UM RANDOM PARA DETERMINAR UM TEMPO PARA CLICK ENTRE INTERVALO MINIMO E MÁXIMO
                                int intervalo = Intervalo(min, max);
                                while(num_anterior == intervalo){
                                    intervalo = Intervalo(min, max);
                                }
                                num_anterior = intervalo;
                                System.out.println("Intervalo ---> "+intervalo);

                                Thread.sleep(intervalo);

                                // #BOTÃO CLICK ENVIO DE FOTO
                                navegador.findElement(By.xpath(XPATH_CLICK_ENVIARFOTO)).click();
                                Thread.sleep(tempo_posEnvioFoto);
                                System.out.println("Enviou !!! ");
                                LISTA_ENVIADOS.add(contato+","+data);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        // Atualiza a interface gráfica com o progresso
                        for (int progress : chunks) {
                            progresso.setValue(progress);
                            lbAndamento.setText(progress + " de " + total_contatos + " enviados...");
                            lbIntervalo.setText("Ultimo intervalo: "+num_anterior);
                        }
                    }

                    @Override
                    protected void done() {
                        // Tarefas a serem executadas após a conclusão da execução
                        // Pode reativar botões, limpar campos, etc.
                        lbIntervalo.setVisible(false);
                        btnPng.setEnabled(true);
                        btnExcel.setEnabled(true);
                        txtMensagem.setEditable(true);
                        btnLimpar.setEnabled(true);
                        btnPasta.setEnabled(true);
                        btnTeste.setEnabled(true);
                        btnEnviar.setEnabled(true);
                        try{
                            if(LISTA_ENVIADOS.size() > 0)   RegistrarEnvios(LISTA_ENVIADOS);
                        } catch ( FileNotFoundException e){
                            JOptionPane.showMessageDialog(null, "Erro ao inserir registro: " + e.getMessage());
                        }
                        
                        // Fecha o navegador após a conclusão
                        navegador.quit();
                    }
                };
                worker.execute();
                lbAndamento.setText("");
                lbIntervalo.setVisible(false);
                
            } catch (HeadlessException e){
                JOptionPane.showMessageDialog(null, "Erro operação gráfica: " + e.getMessage());
            } catch (org.openqa.selenium.NoSuchElementException e) {
                JOptionPane.showMessageDialog(null, "Elemento não encontrado: " + e.getMessage());
            } catch (org.openqa.selenium.TimeoutException e) {
                JOptionPane.showMessageDialog(null, "Tempo limite excedido: " + e.getMessage());
            } catch (org.openqa.selenium.WebDriverException e) {
                JOptionPane.showMessageDialog(null, "Exceção do WebDriver: " + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Exceção genérica: " + e.getMessage());
            }
        }
        
    }
    
    private void TesteNavegador(){
        
        try{
            WebDriver navegador = new ChromeDriver();
            navegador.get("https://www.google.com.br/?hl=pt-BR");
            JOptionPane.showMessageDialog(null, "Navegador Funcionando normalmente!");
            
        } catch (HeadlessException e){
            JOptionPane.showMessageDialog(null, "Erro operação gráfica: " + e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            JOptionPane.showMessageDialog(null, "Elemento não encontrado: " + e.getMessage());
        } catch (org.openqa.selenium.TimeoutException e) {
            JOptionPane.showMessageDialog(null, "Tempo limite excedido: " + e.getMessage());
        } catch (org.openqa.selenium.WebDriverException e) {
            JOptionPane.showMessageDialog(null, "Exceção do WebDriver: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exceção genérica: " + e.getMessage());
        }
        
        
        
    }
    
    private void RegistrarEnvios(ArrayList<String> lista) throws FileNotFoundException {
        
        String formato = "dd-MM-yyyy HH:mm:ss";
        SimpleDateFormat dataMascara = new SimpleDateFormat(formato);
        String data = dataMascara.format(new Date());
        
        String diretorioAreaTrabalho = System.getProperty("user.home") + File.separator + "Desktop";
        
        String nomeArquivo = diretorioAreaTrabalho + File.separator+"Arquivo"+data.replace(":", ".")+".xlsx";
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Criar uma nova planilha
            Sheet sheet = workbook.createSheet("Dados");

            // Iterar sobre a lista e escrever os dados na planilha
            int rowNum = 0;
            for (String item : lista) {
                // Dividir o item da lista usando ","
                String[] partes = item.split(",");
                // Criar uma nova linha na planilha
                Row row = sheet.createRow(rowNum++);
                // Iterar sobre as partes e escrever cada parte em uma coluna
                int colNum = 0;
                for (String parte : partes) {
                    Cell cell = row.createCell(colNum++);
                    cell.setCellValue(parte.trim());
                }
            }

            // Escrever o conteúdo do workbook em um arquivo
            try (FileOutputStream outputStream = new FileOutputStream(nomeArquivo)) {
                workbook.write(outputStream);
            }
            System.out.println("Os dados foram escritos com sucesso no arquivo Excel: " + nomeArquivo);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro: "+e.getMessage());
        }
    }
    
    private void pararProcesso() {
        // Verifique se o processo está em execução e pode ser interrompido
        if (worker != null && !worker.isDone() && !worker.isCancelled()) {
            // Interrompe a execução do SwingWorker
            worker.cancel(true);
            
            try{
                RegistrarEnvios(LISTA_ENVIADOS);
            } catch ( FileNotFoundException e){
                System.out.println("Erro ao inserir registro: "+e.getMessage());
            }
        }
    }
    
    private void PararTodosThreads() {
        
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        Thread[] threads = new Thread[rootGroup.activeCount()];
        while (rootGroup.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }

        for (Thread thread : threads) {
            if (thread != null && thread != Thread.currentThread()) {
                thread.interrupt();
            }
        }

        JOptionPane.showMessageDialog(this, "Software parado.");
        
        lbIntervalo.setVisible(false);
        btnPng.setEnabled(true);
        btnExcel.setEnabled(true);
        txtMensagem.setEditable(true);
        btnLimpar.setEnabled(true);
        btnPasta.setEnabled(true);
        btnTeste.setEnabled(true);
        btnEnviar.setEnabled(true);
    }
    
    private void TesteEnvio(){
        
        if ((dirDriver.getText().isEmpty()) || (dirPng.getText().isEmpty()) || (dirExcel.getText().isEmpty()) || txtMensagem.getText().isEmpty() || txtNumero.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Preencha todos os campos!");
        } else {
            
            // intervalo para range random [min a max]
            int min = Integer.parseInt(tempoMinIntervalo.getText());
            int max = Integer.parseInt(tempoMaxIntervalo.getText());

            // Tempos de espera
            int tempo_posEnvioFoto = Integer.parseInt(tempoPosEnvio.getText());
            
            int tempo_posAtPagina = Integer.parseInt(tempoAtualizacaoPag.getText());
            int tempo_posVerificaNum = Integer.parseInt(tempoVerificacaoNumero.getText());
            int tempo_cliqueOpcoes = Integer.parseInt(tempoCliqueOpcoes.getText());
            int tempo_selecaoFoto = Integer.parseInt(tempoSelecaoFoto.getText());
            
            System.setProperty("webdriver.chrome.driver",dirDriver.getText());
            
            ativado = false;
            String caminhoImagem = dirPng.getText();
            String caminhoExcel = dirExcel.getText();
            String mensagemCodificada = URLEncoder.encode(txtMensagem.getText(), StandardCharsets.UTF_8);
            
            WebDriver navegador = new ChromeDriver();
            
            try {
                navegador.get("https://web.whatsapp.com");
                ativado = true;
            } catch(Exception e){
                System.out.println("Erro: "+e.getMessage());
            }
            
            if(ativado){
                
                try {
                
                    // Aguarda a entrada com QRCode
                    while(navegador.findElements(By.id("side")).isEmpty()){
                        Thread.sleep(200);
                        System.out.println("Carregando");
                    }

                    Thread.sleep(4000);
                    String contato = txtNumero.getText();
                    boolean travou = false;

                    String link = "https://web.whatsapp.com/send?phone="+contato+"&text="+mensagemCodificada;
                    System.out.println("\nLink codificado : "+link);
                    navegador.get(link);

                    while(navegador.findElements(By.id("side")).isEmpty()){
                        Thread.sleep(100); // 1000 milissegundos = 1 segundo
                        System.out.println("Carregando");
                    }
                    Thread.sleep(tempo_posAtPagina); // 1000 milissegundos = 1 segundo
                    System.out.println("------ Verificacao da tela");

                    while((!navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).isEmpty()) && (!travou)){
                        Object elementoObj = navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).get(0);
                        String elemento = elementoObj != null? elementoObj.toString(): "O número de telefone compartilhado por url é inválido.";
                        System.out.println("Aguardando, Texto do elemento: "+elemento);
                        travou = elemento.equals("O número de telefone compartilhado por url é inválido.");
                        Thread.sleep(500); // 1000 milissegundos = 1 segundo
                    }
                    Thread.sleep(tempo_posVerificaNum);

                    System.out.println("Pós atualizacao");

                    if(navegador.findElements(By.xpath("//*[@id=\"app\"]/div/span[2]/div/span/div/div/div/div/div/div[1]")).isEmpty()){
                        System.out.println("Inicia seleção");
                        //# BOTÃO OPÇÕES DE ENVIO
                        navegador.findElement(By.xpath(XPATH_OPCOES)).click();
                        System.out.println("Click opçoes");
                        Thread.sleep(tempo_cliqueOpcoes);

                        // # BOTÃO SELECIONAR FOTO
                        navegador.findElement(By.xpath(XPATH_INPUT)).sendKeys(dirPng.getText());
                        System.out.println("Selecionou foto");
                        Thread.sleep(tempo_selecaoFoto);

                        //REALIZA UM RANDOM PARA DETERMINAR UM TEMPO PARA CLICK ENTRE INTERVALO MINIMO E MÁXIMO
                        int intervalo = Intervalo(min, max);
                        while(num_anterior == intervalo){
                            intervalo = Intervalo(min, max);
                        }
                        num_anterior = intervalo;
                        System.out.println("Intervalo ---> "+intervalo);

                        Thread.sleep(intervalo);

                        // #BOTÃO CLICK ENVIO DE FOTO
                        navegador.findElement(By.xpath(XPATH_CLICK_ENVIARFOTO)).click();
                        Thread.sleep(tempo_posEnvioFoto);
                        System.out.println("Enviou !!! ");

                    }

                    navegador.quit();
                    JOptionPane.showMessageDialog(null, "Concluído!");
                    
                } catch (HeadlessException | InterruptedException e){
                    JOptionPane.showMessageDialog(null, "Erro: "+e.getMessage());
                }   
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao iniciar CHROMEDRIVER!");
            }
            System.out.println("Concluído");
        }
    }
    
    private void LimparCampos(){
        
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja limpar os campos?","Atenção", JOptionPane.YES_NO_OPTION);
        
        if (confirma == JOptionPane.YES_OPTION)
        {
            ((DefaultTableModel) tbContatos.getModel()).setRowCount(0);
            totalContatos.setText("");
            dirPng.setText("");
            dirExcel.setText("");
            txtMensagem.setText("");
            txtNumero.setText("");
            dirDriver.setText("");
            labelImagem.setIcon(null);
            progresso.setValue(0);
            btnPng.setEnabled(true);
            btnExcel.setEnabled(true);
            txtMensagem.setEditable(true);
            btnLimpar.setEnabled(true);
            btnPasta.setEnabled(true);
            btnTeste.setEnabled(true);
            btnEnviar.setEnabled(true);
            
        }
    }
    
    private void onCarregarDadosButtonClick() 
    {
        // Cria e executa uma SwingWorker para realizar a operação em segundo plano
        SwingWorker<Void, Void> worker2 = new SwingWorker<Void, Void>() {
            private JDialog progressDialog;
            @Override
            protected Void doInBackground() throws Exception {
                progressDialog = createProgressDialog(); // Mostra o diálogo de progresso enquanto a operação é executada
                SelecionarExcel();
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose(); // Fecha o diálogo de progresso quando a operação é concluída
            }
        };
        worker2.execute();
    }
    
    private JDialog createProgressDialog() 
    {
        JDialog progressDialog = new JDialog();
        progressDialog.setSize(700, 100);
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setLayout(new BorderLayout());
        JLabel messageLabel = new JLabel("Carregando dados...");
        progressDialog.add(messageLabel, BorderLayout.NORTH);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setResizable(false);
        progressDialog.setVisible(true);
        return progressDialog;
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        painelAzul1 = new com.jormary.projeto_bot_java.PainelAzul();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        btnExcel = new javax.swing.JButton();
        dirExcel = new javax.swing.JTextField();
        btnPng = new javax.swing.JButton();
        dirPng = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMensagem = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbContatos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnLimpar = new javax.swing.JButton();
        btnPasta = new javax.swing.JButton();
        txtNumero = new javax.swing.JTextField();
        btnTeste = new javax.swing.JButton();
        btnParar = new javax.swing.JButton();
        btnEnviar = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        painel = new javax.swing.JPanel();
        labelImagem = new javax.swing.JLabel();
        totalContatos = new javax.swing.JLabel();
        btnDriver = new javax.swing.JButton();
        dirDriver = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tempoAtualizacaoPag = new javax.swing.JTextField();
        tempoVerificacaoNumero = new javax.swing.JTextField();
        tempoCliqueOpcoes = new javax.swing.JTextField();
        tempoSelecaoFoto = new javax.swing.JTextField();
        tempoMinIntervalo = new javax.swing.JTextField();
        tempoMaxIntervalo = new javax.swing.JTextField();
        tempoPosEnvio = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        btnForcarParada = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        progresso = new javax.swing.JProgressBar();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lbAndamento = new javax.swing.JLabel();
        lbIntervalo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bot");

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        btnExcel.setBackground(new java.awt.Color(204, 255, 204));
        btnExcel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone6.png"))); // NOI18N
        btnExcel.setText("Selecione os contatos (Excel)");
        btnExcel.setToolTipText("selecionar contatos");
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcelActionPerformed(evt);
            }
        });

        dirExcel.setEditable(false);
        dirExcel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        btnPng.setBackground(new java.awt.Color(204, 255, 204));
        btnPng.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnPng.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone5.png"))); // NOI18N
        btnPng.setText("Selecione a imagem (PNG)");
        btnPng.setToolTipText("selecionar imagem");
        btnPng.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPng.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPngActionPerformed(evt);
            }
        });

        dirPng.setEditable(false);
        dirPng.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        txtMensagem.setBackground(new java.awt.Color(204, 255, 204));
        txtMensagem.setColumns(20);
        txtMensagem.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        txtMensagem.setLineWrap(true);
        txtMensagem.setRows(5);
        txtMensagem.setToolTipText("Mensagem que será enviada");
        jScrollPane1.setViewportView(txtMensagem);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone3.png"))); // NOI18N
        jLabel1.setText("Mensagem");

        tbContatos = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tbContatos.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbContatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Contatos"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tbContatos);

        jLabel4.setText("Limite de 800 caracteres");

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        btnLimpar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone7.png"))); // NOI18N
        btnLimpar.setText("Limpar campos");
        btnLimpar.setToolTipText("limpar");
        btnLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        btnPasta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone13.png"))); // NOI18N
        btnPasta.setText("Teste Navegador");
        btnPasta.setToolTipText("Teste Navegador");
        btnPasta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPastaActionPerformed(evt);
            }
        });

        btnTeste.setBackground(new java.awt.Color(204, 204, 255));
        btnTeste.setText("Enviar Teste");
        btnTeste.setToolTipText("Enviar mensagem teste");
        btnTeste.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTeste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTesteActionPerformed(evt);
            }
        });

        btnParar.setBackground(new java.awt.Color(255, 153, 153));
        btnParar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone9.png"))); // NOI18N
        btnParar.setText("Parar");
        btnParar.setToolTipText("Para Processo");
        btnParar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnParar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPararActionPerformed(evt);
            }
        });

        btnEnviar.setBackground(new java.awt.Color(0, 204, 204));
        btnEnviar.setText("ENVIAR");
        btnEnviar.setToolTipText("Iniciar envios");
        btnEnviar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });

        jLabel15.setText("Número:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnPasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(5, 5, 5)
                        .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnTeste, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(57, 57, 57)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnParar, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNumero)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTeste, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnParar)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(btnLimpar)
                        .addGap(5, 5, 5)
                        .addComponent(btnPasta)))
                .addGap(5, 5, 5))
        );

        jLabel5.setText("Imagem");

        painel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        labelImagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelImagem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelImagem.setMaximumSize(new java.awt.Dimension(407, 276));
        labelImagem.setMinimumSize(new java.awt.Dimension(407, 276));

        javax.swing.GroupLayout painelLayout = new javax.swing.GroupLayout(painel);
        painel.setLayout(painelLayout);
        painelLayout.setHorizontalGroup(
            painelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 407, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelLayout.setVerticalGroup(
            painelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        totalContatos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalContatos.setText("0 contatos");

        btnDriver.setBackground(new java.awt.Color(204, 255, 204));
        btnDriver.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnDriver.setText("Selecione o CHROMEDRIVER");
        btnDriver.setToolTipText("CHROMEDRIVER");
        btnDriver.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDriverActionPerformed(evt);
            }
        });

        dirDriver.setEditable(false);
        dirDriver.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(totalContatos, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(painel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnPng, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .addComponent(btnExcel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dirExcel)
                            .addComponent(dirPng)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(btnDriver, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dirDriver)))
                .addGap(10, 10, 10))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDriver, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dirDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnPng, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(dirPng, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnExcel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dirExcel))
                .addGap(14, 14, 14)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(totalContatos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(painel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Principal", jPanel6);

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel7.setText("Tempo pós atualização de página");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel8.setText("Tempo pós verificação do número");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Tempo pós clique em \"opções\"");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel10.setText("Tempo pós seleção da foto");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel11.setText("Intervalo para função randômica (Clique para envio)");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel12.setText("min:");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel13.setText("máx:");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel14.setText("Tempo pós envio");

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel9.setBackground(new java.awt.Color(0, 0, 153));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Limitados em máximo 60000 milissegundos\n•\tTempo padrão de atualização da página -> 4000 milissegundos\n•\tTempo padrão de verificação de número -> 2000 milissegundos\n•\tTempo padrão do clique em opções -> 1500 milissegundos\n•\tTempo padrão seleção foto -> 1500 milissegundos\n•\tTempo padrão pós envio -> 3000 milissegundos\n\nAtenção! Tempo mínimo deverá ser menor que tempo máximo.\n•\tTempo mínimo padrão -> 5 segundos\n•\tTempo máximo padrão -> 25 segundos\t\n");
        jScrollPane3.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jScrollPane3)
                .addGap(2, 2, 2))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnForcarParada.setText("Forçar parada");
        btnForcarParada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnForcarParadaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tempoAtualizacaoPag)
                            .addComponent(tempoVerificacaoNumero)
                            .addComponent(tempoCliqueOpcoes)
                            .addComponent(tempoSelecaoFoto)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tempoMinIntervalo)
                                    .addComponent(tempoMaxIntervalo)))
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tempoPosEnvio, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(39, 39, 39)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnForcarParada)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(tempoAtualizacaoPag, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(tempoVerificacaoNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(tempoCliqueOpcoes, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(tempoSelecaoFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tempoMinIntervalo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel13)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(tempoMaxIntervalo, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(tempoPosEnvio, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnForcarParada)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone11.png"))); // NOI18N
        jLabel6.setText("CONFIGURAÇÃO DE TEMPOS DE ENVIO E CLIQUES");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Config.", jPanel5);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/icone10.png"))); // NOI18N
        jLabel2.setText("Progresso");

        progresso.setBackground(new java.awt.Color(255, 255, 255));
        progresso.setForeground(new java.awt.Color(255, 0, 102));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Copperplate Gothic Bold", 0, 24)); // NOI18N
        jLabel3.setIcon(new javax.swing.ImageIcon("C:\\Users\\estagiario3.engenhar\\Desktop\\Software Bot Java\\Projeto_Bot_Java\\src\\main\\resources\\icones\\icone1.png")); // NOI18N
        jLabel3.setText("  Whatsapp bot (java)");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 901, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel3)
                .addGap(8, 8, 8))
        );

        lbAndamento.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lbAndamento.setForeground(new java.awt.Color(255, 255, 255));
        lbAndamento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbAndamento.setText("Parado");

        lbIntervalo.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lbIntervalo.setForeground(new java.awt.Color(255, 255, 255));
        lbIntervalo.setText("Ultimo intervalo: 0");

        javax.swing.GroupLayout painelAzul1Layout = new javax.swing.GroupLayout(painelAzul1);
        painelAzul1.setLayout(painelAzul1Layout);
        painelAzul1Layout.setHorizontalGroup(
            painelAzul1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelAzul1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(painelAzul1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progresso, javax.swing.GroupLayout.PREFERRED_SIZE, 1109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelAzul1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(painelAzul1Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbIntervalo, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(144, 144, 144)
                            .addComponent(lbAndamento, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jTabbedPane1)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(25, 25, 25))
        );
        painelAzul1Layout.setVerticalGroup(
            painelAzul1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelAzul1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(painelAzul1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lbAndamento)
                    .addComponent(lbIntervalo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progresso, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelAzul1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelAzul1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPngActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPngActionPerformed
        SelecionarPNG();
    }//GEN-LAST:event_btnPngActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        onCarregarDadosButtonClick();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        LimparCampos();
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        Inicializacao();
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void btnPararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPararActionPerformed
        pararProcesso();
    }//GEN-LAST:event_btnPararActionPerformed

    private void btnTesteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTesteActionPerformed
        TesteEnvio();
    }//GEN-LAST:event_btnTesteActionPerformed

    private void btnPastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPastaActionPerformed
        TesteNavegador();
    }//GEN-LAST:event_btnPastaActionPerformed

    private void btnForcarParadaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForcarParadaActionPerformed
        PararTodosThreads();
    }//GEN-LAST:event_btnForcarParadaActionPerformed

    private void btnDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDriverActionPerformed
        SelecionaDriver();
    }//GEN-LAST:event_btnDriverActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_Inicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tela_Inicial().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDriver;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnForcarParada;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnParar;
    private javax.swing.JButton btnPasta;
    private javax.swing.JButton btnPng;
    private javax.swing.JButton btnTeste;
    private javax.swing.JTextField dirDriver;
    private javax.swing.JTextField dirExcel;
    private javax.swing.JTextField dirPng;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel labelImagem;
    private javax.swing.JLabel lbAndamento;
    private javax.swing.JLabel lbIntervalo;
    private javax.swing.JPanel painel;
    private com.jormary.projeto_bot_java.PainelAzul painelAzul1;
    private javax.swing.JProgressBar progresso;
    private javax.swing.JTable tbContatos;
    private javax.swing.JTextField tempoAtualizacaoPag;
    private javax.swing.JTextField tempoCliqueOpcoes;
    private javax.swing.JTextField tempoMaxIntervalo;
    private javax.swing.JTextField tempoMinIntervalo;
    private javax.swing.JTextField tempoPosEnvio;
    private javax.swing.JTextField tempoSelecaoFoto;
    private javax.swing.JTextField tempoVerificacaoNumero;
    private javax.swing.JLabel totalContatos;
    private javax.swing.JTextArea txtMensagem;
    private javax.swing.JTextField txtNumero;
    // End of variables declaration//GEN-END:variables
}
