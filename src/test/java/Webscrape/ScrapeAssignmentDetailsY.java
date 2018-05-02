/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Webscrape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 *
 * @author prabakar
 */
class ScrapeAssignmentDetailsY extends PageObject {

    @FindBy(how = How.ID, using = "printable-area")
    public WebElement assignmentOutputArea;

    @FindBy(how = How.ID, using = "resultsTable")
    public WebElement resultTable;

    @FindBy(how = How.XPATH, using = "//*[@id='btn-append-to-body']")
    public WebElement lookupButton;

    //*[@id="btn-append-to-body"]
    @FindBy(how = How.XPATH, using = "//*[@id='quickTxt']")
    public WebElement searchkeyTextbox;

    public ScrapeAssignmentDetailsY(WebDriver driver) {
        super(driver);
    }

    public void getAssignmentDetails() throws FileNotFoundException, IOException, InterruptedException {
        FileInputStream file = new FileInputStream(new File(
                "D:\\jenkins_usassignment\\USAssignmentsearchdatasheet.xlsx"));
        String excelFileName = "D:\\jenkins_usassignment\\USAssignmentoutput.xlsx";//name of excel file
        String sheetName = "USAssignmentDetails";//name of sheetWrite
        XSSFWorkbook workbookWrite = new XSSFWorkbook();
        XSSFSheet sheetWrite = workbookWrite.createSheet(sheetName);
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Cell cell;
        XSSFRow rowWrite;
        Row row = null;
        int rowStart = Math.min(15, sheet.getFirstRowNum());
        int rowEnd = Math.max(1400, sheet.getLastRowNum());
        int rowsraninexcel = 1;
        int totalRowCount = sheet.getPhysicalNumberOfRows()-1;
                
        for (int rowNum = rowStart + 1; rowNum < rowEnd; rowNum++) {
            row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            int columnNumber = 0;
            cell = row.getCell(columnNumber, Row.RETURN_BLANK_AS_NULL);
            if (cell == null) {
            } else {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                RichTextString lookupValue = cell.getRichStringCellValue();
                String lookupItem = lookupValue.toString();
                DataFormatter formatter = new DataFormatter();
                cell = row.getCell(columnNumber + 1, Row.RETURN_BLANK_AS_NULL);
                String inputNumber = formatter.formatCellValue(cell);
                Thread.sleep(1000);
                selectLookupItem(lookupItem);
                enterSearchNumberAndSubmit(inputNumber);
                System.out.println("***** In progress"+ "\n" +"Look up Item " + lookupItem + "--" + inputNumber );
                Thread.sleep(2000);//               
                boolean resultSet = checkResultAvailableOrNot();
                if (resultSet) {
                    WebElement df = assignmentOutputArea;
                    List<WebElement> noOfAssignments = df.findElements(By.tagName("table"));
                    int totalGrid = noOfAssignments.size();
                    int totalNumberOfAssignments = totalGrid - 1;
                    System.out.println("total number of assignment " + totalNumberOfAssignments);                    

                    for (int r = 1; r <= totalNumberOfAssignments; r++) {
                        //System.out.println("rowsraninexcel " + rowsraninexcel);
                        rowWrite = sheetWrite.createRow(rowsraninexcel);
                        String assignee = driver.findElement(By.xpath(" //*[@id='printable-area']/table[" + r + "]/tbody/tr[4]/td/a")).getText();
                        System.out.println("Assignee: "+assignee);
                        String assignors = driver.findElement(By.xpath("//*[@id='printable-area']/table[" + r + "]/tbody/tr[3]/td[1]/a")).getText();
                        System.out.println("Assignor: "+assignors);
                        ArrayList<String> strings = new ArrayList<>();
                        List<WebElement> assigneeAddress = driver.findElements(By.xpath("//*[@id='printable-area']/table[" + r + "]/tbody/tr[4]/td/p[2]/span"));
                        assigneeAddress.stream().forEach((ele) -> {
                            strings.add(ele.getText());
                        });
                        String address = strings.toString().replace("[", "").replace("]", "").trim();
                        System.out.println("Assignor address: "+address);
                        String realFrame = driver.findElement(By.xpath(" //*[@id='printable-area']/table[" + r + "]/tbody/tr[1]/td[1]/a")).getText();
                        System.out.println("Real/Frame: "+realFrame);
                        String executionDate = driver.findElement(By.xpath(" //*[@id='printable-area']/table[" + r + "]/tbody/tr[1]/td[2]")).getText().replace("Execution date", "");
                        System.out.println("Execution Date: "+executionDate);
                        String conveyance = driver.findElement(By.xpath(" //*[@id='printable-area']/table[" + r + "]/tbody/tr[2]/td")).getText().replace("Conveyance", "");
                        System.out.println("Conveyance: "+conveyance);
                        String recordedDate = driver.findElement(By.xpath(" //*[@id='printable-area']/table[" + r + "]/tbody/tr[1]/td[3]")).getText().replace("Date recorded", "");
                        System.out.println("Recorded Date: "+recordedDate);
                        
                        rowWrite.createCell(0).setCellValue(inputNumber);
                        rowWrite.createCell(1).setCellValue(assignee);
                        rowWrite.createCell(2).setCellValue(assignors);
                        rowWrite.createCell(3).setCellValue(address);
                        rowWrite.createCell(4).setCellValue(realFrame);
                        rowWrite.createCell(5).setCellValue(executionDate);
                        if (r == 1) {
                            rowWrite.createCell(6).setCellValue("YES");
                        } else {
                            rowWrite.createCell(6).setCellValue("NO");
                        }
                        rowWrite.createCell(7).setCellValue(conveyance);
                        rowWrite.createCell(8).setCellValue(recordedDate);

                        rowsraninexcel = rowsraninexcel + 1;
                    }
                    driver.findElement(By.linkText("Back to search results")).click();
                } else {
                    System.out.println("Assignment Details not available.");
                    rowWrite = sheetWrite.createRow(rowsraninexcel);
                    rowWrite.createCell(0).setCellValue(inputNumber);
                    rowWrite.createCell(1).setCellValue("Assignment Details not available.");
                    driver.findElement(By.xpath("//a[@href='/patent']"));
                    rowsraninexcel = rowsraninexcel + 1;                    
                }
                System.out.println("Assignments fetching completed  "+inputNumber);
                System.out.println("Completed   "+ row.getRowNum() +"out of"  + totalRowCount);                
            }
            rowWrite = sheetWrite.createRow(0);
            rowWrite.createCell(0).setCellValue("Pat/Pub No");
            rowWrite.createCell(1).setCellValue("Assignee");
            rowWrite.createCell(2).setCellValue("Assignor");
            rowWrite.createCell(3).setCellValue("Address");
            rowWrite.createCell(4).setCellValue("Real/Frame");
            rowWrite.createCell(5).setCellValue("Execution Date");
            rowWrite.createCell(6).setCellValue("Is Current Assignee");
            rowWrite.createCell(7).setCellValue("conveyance");
            rowWrite.createCell(8).setCellValue("Recorded Date");

            //write this workbookWrite to an Outputstream.
            try (FileOutputStream fileOut = new FileOutputStream(excelFileName)) {
                //write this workbookWrite to an Outputstream.
                workbookWrite.write(fileOut);
                fileOut.flush();
            }
        }
    }

    public Boolean checkResultAvailableOrNot() {
        driver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
        boolean checkresultGrid;
        try {
            resultTable.isDisplayed();
            checkresultGrid = true;
        } catch (NoSuchElementException e) {
            checkresultGrid = false;
        } finally {
            driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
        }
        return checkresultGrid;
    }

    public void selectLookupItem(String Item) throws InterruptedException {
        Thread.sleep(2000);
        lookupButton.click();

        WebElement uList = driver.findElement(By.xpath("//*[@id='quick-search-dropdown']/ul"));
        List<WebElement> listCount = uList.findElements(By.tagName("li"));
        for (int i = 1; i <= listCount.size(); i++) {
            WebElement lookupItem = driver.findElement(By.xpath("(//li[@id='quickOption']/a/div/span[1])[" + i + "]"));
            String lookupItemValue = lookupItem.getText();
            if (lookupItemValue.equalsIgnoreCase(Item)) {
                lookupItem.click();
            }
        }
    }

    public void enterSearchNumberAndSubmit(String searchValue) throws InterruptedException {
        this.searchkeyTextbox.clear();
        this.searchkeyTextbox.sendKeys(searchValue);
        this.searchkeyTextbox.sendKeys(Keys.ENTER);
    }
}
