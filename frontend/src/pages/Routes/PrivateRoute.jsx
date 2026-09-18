import { Navigate } from 'react-router-dom'
import { useAuth } from '../Context/AuthContext'

function PrivateRoute({ children }) {
  const { usuario } = useAuth()

  if (!usuario) {
    return <Navigate to="/" replace />
  }

  return children
}

export default PrivateRoute