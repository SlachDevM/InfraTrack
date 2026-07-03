import ExportReportingButton from './ExportReportingButton';
import { REPORTING_EXPORT_FORMATS } from '../constants/reportingExports';

export default function ExportCsvButton(props) {
  return <ExportReportingButton {...props} format={REPORTING_EXPORT_FORMATS.CSV} />;
}
