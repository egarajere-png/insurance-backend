package com.abcbank.insurance.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.abcbank.insurance.entities.Customer;
import com.abcbank.insurance.entities.CustomerProduct;
import com.abcbank.insurance.entities.Dependant;
import com.abcbank.insurance.entities.PersonType;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.services.DependantService;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

import com.itextpdf.text.Image;

@Slf4j
@Service
public class ApplicationPDFGenerator {

	private String filePath;

	private static Font headingFont = new Font(Font.FontFamily.HELVETICA, 12,
			Font.BOLD, new BaseColor(255, 255, 255));

	private static Font blueFontUnderline = new Font(Font.FontFamily.HELVETICA, 10,
			Font.UNDERLINE, new BaseColor(0, 0, 139));

	private static Font blueFontBold = new Font(Font.FontFamily.HELVETICA, 11,
			Font.BOLD, new BaseColor(0, 0, 139));

	private static Font tinyWhiteBold = new Font(Font.FontFamily.HELVETICA, 9,
			Font.BOLD, new BaseColor(255, 255, 255));

	private static Font tiny = new Font(Font.FontFamily.HELVETICA, 9);

	private static Font tinyGrey = new Font(Font.FontFamily.HELVETICA, 9,
			Font.NORMAL, new BaseColor(50, 50, 50));

	private static Font small = new Font(Font.FontFamily.HELVETICA, 11);

	private static Font micro = new Font(Font.FontFamily.HELVETICA, 8,
			Font.NORMAL);

	private static Font microBold = new Font(Font.FontFamily.HELVETICA, 8,
			Font.BOLD);

	private static Font microGrey = new Font(Font.FontFamily.HELVETICA, 8,
			Font.NORMAL, new BaseColor(50, 50, 50));

	private static Font tinyItalic = new Font(Font.FontFamily.HELVETICA, 9,
			Font.BOLDITALIC);

	@Autowired
	private DependantService dService;

	/**
	 * Set for the duration of a single generateApplicationPDF() call.
	 */
	private CustomerProduct customerProduct;

	/**
	 * Generates the application PDF.
	 *
	 * The PDF document MUST be closed while the FileOutputStream is still
	 * open. If the stream is closed first, iText throws:
	 *
	 * com.itextpdf.text.ExceptionConverter: Stream Closed
	 */
	public synchronized File generateApplicationPDF(
			CustomerProduct customerProduct) {

		if (customerProduct == null
				|| customerProduct.getCustomer() == null) {

			log.error(
					"Cannot generate PDF: no application/customer given"
			);

			return null;
		}

		if (customerProduct.getCustomer().getDateOfBirth() == null) {

			log.error(
					"Cannot generate PDF: customer {} has no date of birth on file",
					customerProduct.getCustomer().getIdNumber()
			);

			return null;
		}

		/*
		 * A nominated beneficiary is optional.
		 */
		this.customerProduct = customerProduct;

		filePath = "/tmp/"
				+ customerProduct.getCustomer().getIdNumber()
				+ "-"
				+ customerProduct.getId()
				+ ".pdf";

		log.info("File path {}", filePath);

		Document document = new Document();

		try (FileOutputStream out =
				new FileOutputStream(filePath)) {

			document.setMargins(25, 25, 25, 25);

			/*
			 * One PdfWriter for the whole document.
			 */
			PdfWriter.getInstance(document, out);

			document.open();

			addMetaData(document);
			addImage(document);
			addLetterheadAddress(document);
			addHeading(document);
			addGoupName(document);
			addSectionPrincipal(document);
			addSectionNominated(document);
			addSectionPlans(document);
			addSectionDependants(document);
			addSectionHealthStatement(document);
			addSectionDeclaration(document);
			addSectionTermsSummary(document);

			/*
			 * IMPORTANT:
			 *
			 * Close the iText document BEFORE the try-with-resources
			 * closes the FileOutputStream.
			 *
			 * iText needs the stream to still be open when it flushes
			 * the final PDF structure.
			 */
			document.close();

		} catch (Exception e) {

			log.error(
					"Failed to generate application PDF",
					e
			);

			/*
			 * If something failed before document.close(), make a best
			 * effort to close the iText document.
			 */
			if (document.isOpen()) {

				try {
					document.close();
				} catch (Exception closeException) {

					log.error(
							"Failed to close PDF document cleanly",
							closeException
					);
				}
			}

			return null;
		}

		File generatedFile = new File(filePath);

		if (!generatedFile.exists()) {

			log.error(
					"PDF generation completed but file does not exist: {}",
					filePath
			);

			return null;
		}

		if (generatedFile.length() == 0) {

			log.error(
					"PDF generation completed but generated file is empty: {}",
					filePath
			);

			return null;
		}

		log.info(
				"Application PDF generated successfully: {} ({} bytes)",
				filePath,
				generatedFile.length()
		);

		return generatedFile;
	}

	private void addMetaData(Document document) {

		document.addTitle(
				"ABC Bank PDF Document"
		);

		document.addSubject(
				"ABC Insurance Application PDF"
		);

		document.addKeywords(
				"ABCIB, Application, Kwaheri"
		);

		document.addAuthor(
				"Samuel Waithaka"
		);

		document.addCreator(
				"Samuel Waithaka"
		);
	}

	private void addLetterheadAddress(
			Document document)
			throws DocumentException {

		Paragraph preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"ABC Insurance Brokers Limited, ABC Bank House, 3rd Floor, East Wing",
						small
				)
		);

		preface.add(
				new Paragraph(
						"Woodvale Grove, Westlands, P.O. Box 13756 - 00800, Nairobi",
						small
				)
		);

		preface.add(
				new Paragraph(
						"Office Cell: 0728606545 / 0722200476 / 0734200476",
						small
				)
		);

		preface.add(
				new Paragraph(
						"Email: insurance@abcthebank.com",
						small
				)
		);

		addEmptyLine(
				preface,
				2
		);

		document.add(preface);
	}

	private void addHeading(
			Document document)
			throws BadElementException, DocumentException {

		PdfPTable table =
				new PdfPTable(1);

		table.setWidthPercentage(100);

		PdfPCell pcell =
				new PdfPCell();

		pcell.setBorderWidth(0.1f);

		pcell.setPaddingBottom(10);

		pcell.setPaddingLeft(50);

		pcell.setBackgroundColor(
				new BaseColor(
						50,
						50,
						139
				)
		);

		pcell.addElement(
				new Paragraph(
						"ABC KWAHERI PLAN LAST EXPENSE INSURANCE PROPOSAL FORM",
						headingFont
				)
		);

		table.addCell(pcell);

		document.add(table);

		addEmptyLine(
				new Paragraph(""),
				3
		);
	}

	private void addGoupName(
			Document document)
			throws BadElementException, DocumentException {

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		document.add(preface);

		PdfPTable table =
				new PdfPTable(25);

		table.setHorizontalAlignment(0);

		table.setWidthPercentage(100);

		table.addCell(
				getCellPro(
						3,
						"Group Name:",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		table.addCell(
				getCell(
						10,
						"",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCellPro(
						1,
						"",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		table.addCell(
				getCellPro(
						5,
						"Date of Joining Group:",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		table.addCell(
				getCell(
						6,
						"",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		document.add(table);
	}

	private void addSectionPrincipal(
			Document document)
			throws DocumentException {

		Customer customer =
				customerProduct.getCustomer();

		String dob =
				customer.getDateOfBirth() == null
						? ""
						: new SimpleDateFormat(
								"dd/MM/yyyy"
						).format(
								customer.getDateOfBirth()
						);

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"PART A - PRINCIPLE MEMBER",
						blueFontUnderline
				)
		);

		addEmptyLine(
				preface,
				1
		);

		PdfPTable table =
				new PdfPTable(12);

		table.setWidthPercentage(100);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.addCell(
				getCell(
						2,
						"Full Name:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						10,
						customer.getName(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Date of Birth:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						dob,
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"ID Number:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						customer.getIdNumber(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						"Gender:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						3,
						customer.getGender(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"PIN Number:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						customer.getPinNumber(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Occupation:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						6,
						customer.getOccupation(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Mobile Number:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						customer.getMobileNumber(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Email:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						6,
						customer.getEmailAddress(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"P.O Box:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						customer.getPostalAddress(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Code:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						customer.getPostalCode(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						"Town:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						3,
						customer.getCity(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.completeRow();

		document.add(preface);

		document.add(table);
	}

	private void addSectionNominated(
			Document document)
			throws DocumentException {

		Dependant nominated =
				dService.getNominatedBeneficiary(
						customerProduct.getCustomer(),
						PersonType.NOMINATED
				);

		String dob =
				nominated == null
						|| nominated.getDateOfBirth() == null
						? ""
						: new SimpleDateFormat(
								"dd/MM/yyyy"
						).format(
								nominated.getDateOfBirth()
						);

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"PART B - NOMINATED BENEFICIARY",
						blueFontUnderline
				)
		);

		addEmptyLine(
				preface,
				1
		);

		PdfPTable table =
				new PdfPTable(12);

		table.setWidthPercentage(100);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.addCell(
				getCell(
						2,
						"Full Name:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						10,
						nominated == null
								? ""
								: nominated.getName(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"ID Number:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						nominated == null
								? ""
								: nominated.getIdNumber(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Date of Birth:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						dob,
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Relationship:",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						nominated == null
								? ""
								: nominated.getRelationship(),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.completeRow();

		document.add(preface);

		document.add(table);
	}

	private void addSectionPlans(
			Document document)
			throws DocumentException {

		Product product =
				customerProduct.getProduct();

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"BENEFIT PLANS",
						blueFontBold
				)
		);

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"Selected Plan: Kwaheri Plan "
								+ product.getPlan(),
						blueFontUnderline
				)
		);

		addEmptyLine(
				preface,
				1
		);

		PdfPTable table =
				new PdfPTable(3);

		table.setWidthPercentage(50);

		table.setHorizontalAlignment(0);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.getDefaultCell()
				.setBorderColor(
						new BaseColor(
								100,
								100,
								100
						)
				);

		table.addCell(
				getCell(
						2,
						"CATEGORY:",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						1,
						product.getName(),
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						2,
						"Principal Member:",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getBenefit()
						),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Spouse (1):",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getBenefit()
						),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Children (4)",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getBenefitChild()
						),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Parents and Parents-in-law (1):",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getBenefit()
						),
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Claim Basis",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						"First 2 deaths",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Annual Premium (Per Family):",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		/*
		 * Product.premium is an int, so it cannot be null.
		 * DecimalFormat.format(int) is therefore used directly.
		 */
		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getPremium()
						),
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Annual Premium Per Additional Child (<=18 years):",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getPremiumAdditionalChild()
						),
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						2,
						"Annual Premium Per Additional Adult Child (>18 years)",
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		table.addCell(
				getCell(
						1,
						new DecimalFormat(
								"###,###,###"
						).format(
								product.getPremiumAdditionalAdultChild()
						),
						new BaseColor(
								246,
								220,
								172
						),
						tiny
				)
		);

		document.add(preface);

		table.completeRow();

		document.add(table);

		preface =
				new Paragraph();

		preface.setAlignment(2);

		String basicPlanTerms =
				"* This premium covers the Principal Member and up to 1 Spouse, "
				+ "4 Children, 2 Parents and 2 Parents in-law. Additional "
				+ "members exceeding the maximum limit per category will be "
				+ "required to pay the additional premium provided. With the "
				+ "additional premium paid, the total number of members that "
				+ "can be covered per family is 10.";

		String proPlanTerms =
				"* This premium covers the Principal Member and up to 1 Spouse, "
				+ "4 Children, 4 Siblings, 2 Parents and 2 Parents in-law. "
				+ "Additional members exceeding the maximum limit per category "
				+ "will be required to pay the additional premium provided. "
				+ "With the additional premium paid, the total number of "
				+ "members that can be covered per family is 14.";

		String planTerms =
				basicPlanTerms;

		planTerms =
				product.getPlan().name().equals("Pro")
						? proPlanTerms
						: planTerms;

		preface.add(
				new Paragraph(
						planTerms,
						micro
				)
		);

		addEmptyLine(
				preface,
				6
		);

		document.add(preface);

		table =
				new PdfPTable(1);

		table.setWidthPercentage(100);

		table.setHorizontalAlignment(0);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.getDefaultCell()
				.setBorderColor(
						new BaseColor(
								100,
								100,
								100
						)
				);

		table.addCell(
				getCell(
						1,
						"Underwritten by APA Life Assurance Kenya Limited",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.completeRow();

		document.add(table);
	}

	private void addSectionDependants(
			Document document)
			throws DocumentException {

		Paragraph preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"PART C - DEPENDENTS",
						blueFontUnderline
				)
		);

		addEmptyLine(
				preface,
				1
		);

		PdfPTable table =
				new PdfPTable(14);

		table.setWidthPercentage(100);

		table.setHorizontalAlignment(0);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.getDefaultCell()
				.setBorderColor(
						new BaseColor(
								100,
								100,
								100
						)
				);

		table.addCell(
				getCell(
						1,
						"",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						5,
						"Full Names",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						2,
						"Date of Birth",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						3,
						"Relationship",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.addCell(
				getCell(
						3,
						"Mobile Number",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		List<Dependant> dependants =
				dService.getCustomerDependants(
						customerProduct.getCustomer()
				);

		for (int i = 0; i < 10; i++) {

			Dependant dependant =
					null;

			try {

				dependant =
						dependants.get(i);

			} catch (IndexOutOfBoundsException e) {
				/*
				 * No dependant exists for this row.
				 */
			}

			String name = "";
			String dob = "";
			String relationship = "";
			String mobileNumber = "";

			if (dependant != null) {

				name =
						dependant.getName();

				dob =
						dependant.getDateOfBirth() == null
								? ""
								: new SimpleDateFormat(
										"dd/MM/yyyy"
								).format(
										dependant.getDateOfBirth()
								);

				relationship =
						dependant.getRelationship();

				mobileNumber =
						dependant.getMobileNumber();
			}

			table.addCell(
					getCell(
							1,
							Integer.toString(i + 1),
							new BaseColor(
									255,
									255,
									255
							),
							tiny
					)
			);

			table.addCell(
					getCell(
							5,
							name,
							new BaseColor(
									255,
									255,
									255
							),
							tiny
					)
			);

			table.addCell(
					getCell(
							2,
							dob,
							new BaseColor(
									255,
									255,
									255
							),
							tiny
					)
			);

			table.addCell(
					getCell(
							3,
							relationship,
							new BaseColor(
									255,
									255,
									255
							),
							tiny
					)
			);

			table.addCell(
					getCell(
							3,
							mobileNumber,
							new BaseColor(
									255,
									255,
									255
							),
							tiny
					)
			);
		}

		document.add(preface);

		table.completeRow();

		document.add(table);

		preface =
				new Paragraph();

		preface.setAlignment(2);

		String planTerms =
				"Note: Dependents not listed above at the time of application "
				+ "will not be covered, unless added through an endorsement "
				+ "issued by APA";

		preface.add(
				new Paragraph(
						planTerms,
						tiny
				)
		);

		document.add(preface);
	}

	private void addSectionHealthStatement(
			Document document)
			throws DocumentException {

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"STATEMENT OF HEALTH OF THE LIVES ASSURED",
						blueFontUnderline
				)
		);

		boolean goodHealth =
				customerProduct.isInGoodHealth();

		String yesNo =
				goodHealth == true
						? "Yes"
						: "No";

		preface.add(
				new Paragraph(
						"1. Are you and your dependants in good health? "
								+ yesNo,
						tinyGrey
				)
		);

		if (goodHealth == false) {

			preface.add(
					new Paragraph(
							"If no, please explain",
							tinyGrey
					)
			);

			preface.add(
					new Paragraph(
							customerProduct.getHealthStatus(),
							tiny
					)
			);
		}

		addEmptyLine(
				preface,
				1
		);

		boolean speficicDiagnosis =
				customerProduct.isSpecificDiasgnosis();

		yesNo =
				speficicDiagnosis == true
						? "Yes"
						: "No";

		String history =
				"2. Have you or any of your dependents been diagnosed with, "
				+ "currently receiving treatment for any chronic or terminal "
				+ "illness or have required long-term medical care? "
				+ yesNo;

		preface.add(
				new Paragraph(
						history,
						tinyGrey
				)
		);

		if (speficicDiagnosis == true) {

			preface.add(
					new Paragraph(
							"If yes, please explain",
							tinyGrey
					)
			);

			preface.add(
					new Paragraph(
							customerProduct.getSpecificDiasgnosisStatus(),
							tiny
					)
			);
		}

		String note =
				"Noted: Any material medical history that is omitted from this "
				+ "Proposal Form may invalidate the cover and any claim on the policy.";

		preface.add(
				new Paragraph(
						note,
						tinyItalic
				)
		);

		document.add(preface);
	}

	private void addSectionDeclaration(
			Document document)
			throws DocumentException {

		String name =
				customerProduct.getCustomer().getName();

		Paragraph preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		preface.add(
				new Paragraph(
						"DECLARATION BY PRINCIPAL MEMBER",
						blueFontUnderline
				)
		);

		String declaration =
				"I %s declare that the information provided in this Proposal "
				+ "Form, whether in my own handwriting or not, are true and "
				+ "complete and shall form the basis of the insurance contract "
				+ "between APA Life Assurance Kenya Limited and myself. I "
				+ "further declare that I have disclosed all material facts "
				+ "which may affect the assessment of this proposal. I "
				+ "understand that any misrepresentation or non-disclosure of "
				+ "information could result in my insurance cover being rendered "
				+ "null and void. I understand that this insurance cover is "
				+ "subject to the policy terms and conditions, which I can "
				+ "request for from ABC Insurance Brokers, and a summary of "
				+ "which has been provided below.";

		declaration =
				String.format(
						declaration,
						name
				);

		preface.add(
				new Paragraph(
						declaration,
						tinyGrey
				)
		);

		addEmptyLine(
				preface,
				1
		);

		PdfPTable table =
				new PdfPTable(14);

		table.setWidthPercentage(100);

		table.setHorizontalAlignment(0);

		table.addCell(
				getCellPro(
						2,
						"Signature:",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		table.addCell(
				getCell(
						4,
						"",
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		table.addCell(
				getCellPro(
						3,
						"",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		table.addCell(
				getCellPro(
						1,
						"Date:",
						new BaseColor(
								255,
								255,
								255
						),
						tiny,
						0
				)
		);

		String today =
				new SimpleDateFormat(
						"dd/MM/yyyy"
				).format(
						new Date(
								System.currentTimeMillis()
						)
				);

		table.addCell(
				getCell(
						4,
						today,
						new BaseColor(
								255,
								255,
								255
						),
						tiny
				)
		);

		document.add(preface);

		document.add(table);

		preface =
				new Paragraph();

		addEmptyLine(
				preface,
				1
		);

		document.add(preface);
	}

	private void addSectionTermsSummary(
			Document document)
			throws DocumentException {

		PdfPTable table =
				new PdfPTable(18);

		table.setHorizontalAlignment(0);

		table.setWidthPercentage(100);

		table.addCell(
				getCell(
						18,
						"SUMMARY OF THE POLICY TERMS AND CONDITIONS",
						new BaseColor(
								255,
								255,
								255
						),
						blueFontBold
				)
		);

		table.addCell(
				getCell(
						4,
						"Maximum Entry Age (Adults)",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						8,
						"Adults - None",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Terminal Age",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						2,
						"90 Years",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Minimum Entry Age (Children)",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						2,
						"1 Month",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Maximum Entry Age (Children)",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						2,
						"18 Years",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Terminal Age (Children)",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						2,
						"25 Years",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"No of Claims Per Year",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						14,
						"First 2 deaths - Maximum of 2 deaths per year",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Catastrophic Deaths",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						14,
						"Not applicable",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		table.addCell(
				getCell(
						4,
						"Waiting Periods",
						new BaseColor(
								255,
								255,
								255
						),
						microGrey
				)
		);

		Paragraph preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"* Waiting periods:",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* Principle Member, Spouse, Children, Siblings, Parents and Parents-in-law: 90 Days",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* No waiting periods for accidents",
						microGrey
				)
		);

		table.addCell(
				getParagraphCell(
						14,
						preface,
						new BaseColor(
								255,
								255,
								255
						)
				)
		);

		preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"Membership Requirements",
						microBold
				)
		);

		preface.add(
				new Paragraph(
						"* Copy of ID/Passport",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* Birth Certificate (for Children under 18 years)",
						microGrey
				)
		);

		table.addCell(
				getParagraphCell(
						4,
						preface,
						new BaseColor(
								255,
								255,
								255
						)
				)
		);

		preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"Claim Requirements",
						microBold
				)
		);

		preface.add(
				new Paragraph(
						"* Claim form",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* Certified copy of burial permit",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* Certified copy of identity document of the member",
						microGrey
				)
		);

		preface.add(
				new Paragraph(
						"* Certified copy of identity document of the beneficiary",
						microGrey
				)
		);

		table.addCell(
				getParagraphCell(
						14,
						preface,
						new BaseColor(
								255,
								255,
								255
						)
				)
		);

		document.add(table);

		String note =
				"Note: Fully documented claims are payable within 48 hours";

		preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						note,
						tinyItalic
				)
		);

		addEmptyLine(
				preface,
				1
		);

		document.add(preface);

		table =
				new PdfPTable(1);

		table.setWidthPercentage(100);

		table.setHorizontalAlignment(0);

		table.getDefaultCell()
				.setBorderWidth(1);

		table.getDefaultCell()
				.setBorderColor(
						new BaseColor(
								100,
								100,
								100
						)
				);

		table.addCell(
				getCell(
						1,
						"Underwritten by APA Life Assurance Kenya Limited",
						new BaseColor(
								50,
								50,
								139
						),
						tinyWhiteBold
				)
		);

		table.completeRow();

		document.add(table);
	}

	private void addEmptyLine(
			Paragraph paragraph,
			int number) {

		for (int i = 0; i < number; i++) {

			paragraph.add(
					new Paragraph(" ")
			);
		}
	}

	private void drawLine(
			Document document)
			throws DocumentException {

		Paragraph preface =
				new Paragraph();

		preface.add(
				new Paragraph(
						"__________________________________________________________________________",
						small
				)
		);

		document.add(preface);
	}

	private void addImage(
			Document document) {

		try (
				InputStream abcLogo =
						new ClassPathResource(
								"images/abcib-logo.jpg"
						).getInputStream()
		) {

			Image image2 =
					Image.getInstance(
							FileCopyUtils.copyToByteArray(
									abcLogo
							)
					);

			image2.scaleAbsoluteHeight(75);

			image2.scaleAbsoluteWidth(80);

			image2.setAbsolutePosition(
					492f,
					738f
			);

			document.add(image2);

		} catch (Exception e) {

			log.error(
					"Failed to add letterhead logo to PDF: {}",
					e.getMessage()
			);
		}
	}

	private PdfPCell getCell(
			int cm,
			String content,
			BaseColor backgroundColor,
			Font font) {

		PdfPCell cell =
				new PdfPCell();

		cell.setBackgroundColor(
				backgroundColor
		);

		cell.setBorderColor(
				new BaseColor(
						150,
						150,
						150
				)
		);

		cell.setColspan(cm);

		cell.setUseAscender(true);

		cell.setUseDescender(true);

		cell.setBorderWidth(0.5f);

		cell.setPadding(3);

		Paragraph p =
				new Paragraph(
						content == null
								? ""
								: content,
						font
				);

		cell.addElement(p);

		return cell;
	}

	private PdfPCell getParagraphCell(
			int cm,
			Paragraph paragraph,
			BaseColor backgroundColor) {

		PdfPCell cell =
				new PdfPCell();

		cell.setBackgroundColor(
				backgroundColor
		);

		cell.setBorderColor(
				new BaseColor(
						150,
						150,
						150
				)
		);

		cell.setColspan(cm);

		cell.setUseAscender(true);

		cell.setUseDescender(true);

		cell.setBorderWidth(0.5f);

		cell.setPadding(3);

		cell.addElement(paragraph);

		return cell;
	}

	private PdfPCell getCellPro(
			int cm,
			String content,
			BaseColor backgroundColor,
			Font font,
			float borderWidth) {

		PdfPCell cell =
				new PdfPCell();

		cell.setBackgroundColor(
				backgroundColor
		);

		cell.setBorderColor(
				new BaseColor(
						150,
						150,
						150
				)
		);

		cell.setColspan(cm);

		cell.setUseAscender(true);

		cell.setUseDescender(true);

		cell.setBorderWidth(borderWidth);

		cell.setPadding(3);

		Paragraph p =
				new Paragraph(
						content == null
								? ""
								: content,
						font
				);

		cell.addElement(p);

		return cell;
	}
}