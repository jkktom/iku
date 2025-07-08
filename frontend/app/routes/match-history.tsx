import { useEffect } from "react";
import { useNavigate } from "@remix-run/react";

export default function MatchHistory() {
  const navigate = useNavigate();

  useEffect(() => {
    // Redirect to single match history as default
    navigate('/single-match-history', { replace: true });
  }, [navigate]);

  return null;
}