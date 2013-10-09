import os, sys, xlrd

try:
    filename=os.environ["filepath"]
except KeyError:
    print "Please provide filepath required"
    sys.exit(1)

def extractTextFromExcel(path):
    print path
    text = ''
    f = open(path)
    workbook = xlrd.open_workbook(file_contents=f.read())
    # workbook = xlrd.open_workbook(path);
    worksheets = workbook.sheet_names()
    for worksheet_name in worksheets:
        worksheet = workbook.sheet_by_name(worksheet_name)
        num_rows = worksheet.nrows - 1

        curr_row = -1
        while curr_row < num_rows:
            curr_row += 1
            row = worksheet.row(curr_row)

            curr_cell = -1
            num_cells = len(row) - 1
            while curr_cell < num_cells:
                curr_cell += 1
                #print curr_cell
                # Cell Types: 0=Empty, 1=Text, 2=Number, 3=Date, 4=Boolean, 5=Error, 6=Blank
                cell_type = worksheet.cell_type(curr_row, curr_cell)
                cell_value = worksheet.cell_value(curr_row, curr_cell)
                #print cell_value
                if cell_type == 1:
                    text += ' ' + cell_value
        #f.close();
    return text

print "Yellow "+os.getcwd()
print extractTextFromExcel(filename)
