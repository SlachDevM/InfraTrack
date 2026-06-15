import JobCard from './JobCard';
import '../styles/JobList.css';

export default function JobList({ jobs, onJobClick }) {
  return (
    <div className="job-list">
      {jobs.length === 0 ? (
        <div className="no-jobs">No pending or to-be-fixed jobs.</div>
      ) : (
        <div className="job-cards">
          {jobs.map((job) => (
            <JobCard key={job.id} job={job} onJobClick={onJobClick} />
          ))}
        </div>
      )}
    </div>
  );
}
