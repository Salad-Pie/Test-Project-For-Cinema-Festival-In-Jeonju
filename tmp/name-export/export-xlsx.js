const fs = require('fs');
const path = require('path');
const XLSX = require('xlsx');

const csvPath = path.resolve('C:/Users/dldbs/Desktop/Test-Project-For-Cinema-Festival-In-Jeonju/tmp/name-export/name-conversion-results.csv');
const outPath = path.resolve('C:/Users/dldbs/Downloads/english-name-to-korean-test-results.xlsx');
const csv = fs.readFileSync(csvPath, 'utf8');
const workbook = XLSX.read(csv, { type: 'string' });
const sheet = workbook.Sheets[workbook.SheetNames[0]];
if (!sheet['!cols']) {
  sheet['!cols'] = [{ wch: 8 }, { wch: 36 }, { wch: 30 }, { wch: 20 }, { wch: 10 }];
}
XLSX.writeFile(workbook, outPath);
console.log(outPath);