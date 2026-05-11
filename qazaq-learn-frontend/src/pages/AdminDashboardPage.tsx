import { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { AuditLog } from '../types';
import { auditApi } from '../api/auditApi';
import { errorUtils } from '../utils/auth';

export const AdminDashboardPage = () => {
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAuditLogs = async () => {
      try {
        const logsData = await auditApi.getAuditLogs();
        setAuditLogs(logsData);
      } catch (err) {
        setError(errorUtils.getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchAuditLogs();
  }, []);

  if (loading) return <Layout><LoadingSpinner /></Layout>;

  return (
    <Layout>
      <div className="space-y-8">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Әкімші панелі</h1>
          <p className="text-gray-600">Түпсін журналы</p>
        </div>

        {error && <ErrorMessage message={error} />}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Email</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Әрекет</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Түрі</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">ID</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">Уақыты</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700">IP Мекенжайы</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id} className="border-t hover:bg-gray-50">
                  <td className="px-6 py-3 text-sm text-gray-700">{log.actorEmail}</td>
                  <td className="px-6 py-3 text-sm text-gray-700">{log.action}</td>
                  <td className="px-6 py-3 text-sm text-gray-700">{log.entityType}</td>
                  <td className="px-6 py-3 text-sm text-gray-700">{log.entityId}</td>
                  <td className="px-6 py-3 text-sm text-gray-700">
                    {new Date(log.timestamp).toLocaleString('kk-KZ')}
                  </td>
                  <td className="px-6 py-3 text-sm text-gray-700">{log.ipAddress}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {auditLogs.length === 0 && (
            <div className="px-6 py-8 text-center text-gray-600">
              Түпсін журналы бос
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};
