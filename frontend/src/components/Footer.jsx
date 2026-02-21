export default function Footer() {
    return (
        <footer className="footer" role="contentinfo">
            <div className="container">
                <p className="footer-disclaimer">
                    ⚠️ <strong>Disclaimer:</strong> GovtJobs.in is an independent aggregator. We do not own, host, or modify any job notifications.
                    All listings are sourced directly from official Indian Government, Banking, PSU, and State Government websites.
                    Always verify details on the official source before applying. We are not responsible for any inaccuracies.
                </p>
                <div className="footer-links">
                    <a href="https://ssc.gov.in" target="_blank" rel="noopener noreferrer">SSC</a>
                    <a href="https://www.ibps.in" target="_blank" rel="noopener noreferrer">IBPS</a>
                    <a href="https://bank.sbi" target="_blank" rel="noopener noreferrer">SBI</a>
                    <a href="https://upsc.gov.in" target="_blank" rel="noopener noreferrer">UPSC</a>
                    <a href="https://www.tnpsc.gov.in" target="_blank" rel="noopener noreferrer">TNPSC</a>
                    <a href="https://www.rrbcdg.gov.in" target="_blank" rel="noopener noreferrer">RRB</a>
                    <a href="https://employmentnews.gov.in" target="_blank" rel="noopener noreferrer">Employment News</a>
                </div>
                <p className="footer-copy">
                    © {new Date().getFullYear()} GovtJobs.in · Made for India 🇮🇳 · Dindigul, Tamil Nadu
                </p>
            </div>
        </footer>
    );
}
